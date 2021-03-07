package io.lroyia.demo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * OCR预处理demo
 * 思路：
 * 1、确认验证码图片长宽，指定局部扫描区域
 * 2、扫描局部区域，根据像素点颜色比例，找出背景色和干扰线的颜色。
 * 3、扫描全图，将于干扰线颜色相近的的所有像素点转为背景色，将非背景颜色色和非干扰线颜色色的像素点转为黑色，达成去像素点。
 *
 * @author <a href="https://blog.lroyia.top">lroyia</a>
 * @since 2021/3/7 16:03
 **/
public class OCRDemo {

    public static void main(String[] args) {

        // 1、读取图片
        BufferedImage image;
        try (FileInputStream fis = new FileInputStream("D:/checkCode.jpg")) {
            image = ImageIO.read(fis);
        } catch (IOException exception) {
            exception.printStackTrace();
            return;
        }

        /*
        2、处理图片
        处理目的：清除干扰线，将文字全部设为黑色
        处理思路：以图片1/4高度为宽, 图片高度为高的的长方形区域，检索图片左边区域
        检出出现最多的颜色近似色为背景色，最少的为干扰线颜色
        最后，扫描全图，根据剔除干扰线，改变文字颜色
         */
        int width = image.getWidth();
        int height = image.getHeight();

        // 计算检索区域边长
        int len = image.getHeight() / 4;

        // 设定限制距离
        int limitD = 50;

        // 统计颜色Map
        Map<Pixel, Integer> summaryMap = new HashMap<>();
        Map<Pixel, ArrayList<Pixel>> similarMap = new HashMap<>();

        // 采样
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < height; j++) {
                int count = 1;
                Pixel pixel = getPixel(image, i, j);
                Pixel similar = null;
                for (Pixel each : summaryMap.keySet()) {
                    if (each.isSimilar(pixel, 50)) {
                        similar = each;
                        count = summaryMap.get(similar) + 1;
                        break;
                    }
                }
                if (similar != null) {
                    summaryMap.put(similar, count);
                    similarMap.get(similar).add(pixel);
                }else{
                    summaryMap.put(pixel, count);
                    similarMap.put(pixel, new ArrayList<>());
                }
            }
        }
        if (summaryMap.size() < 1) {
            System.out.println("扫描区域过小，请扩大扫描区域");
        }

        // 找出背景色和干扰线颜色
//        Pixel maxPixel = null;
//        Pixel minPixel = null;
//        int max = 0;
//        int min = Integer.MAX_VALUE;
//        for (Map.Entry<Pixel, Integer> each : summaryMap.entrySet()) {
//            Pixel pixel = each.getKey();
//            int value = each.getValue();
//            if (value > max) {
//                maxPixel = pixel;
//                max = value;
//            }
//            if (value < min) {
//                minPixel = pixel;
//                min = value;
//            }
//        }
        Pixel maxPixel;
        Pixel minPixel;

        List<Pixel> allPix = new ArrayList<>(summaryMap.keySet());
        allPix.sort(Comparator.comparing(summaryMap::get).reversed());
        maxPixel = allPix.get(0);
        minPixel = allPix.get(1);

        if(similarMap.get(maxPixel) != null){
            ArrayList<Pixel> list = similarMap.get(maxPixel);
            maxPixel = maxPixel.midPixel(list.toArray(new Pixel[0]));
        }
        if(similarMap.get(minPixel) != null){
            ArrayList<Pixel> list = similarMap.get(minPixel);
            minPixel = minPixel.midPixel(list.toArray(new Pixel[0]));
        }

        // 改色
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Pixel pixel = getPixel(image, i, j);
                if (pixel.isSimilar(minPixel, limitD)) {
                    // 将干扰线设为背景色
                    image.setRGB(i, j, maxPixel.getColor());
                } else if (!pixel.isSimilar(maxPixel, limitD)) {
                    // 文字设黑
                    image.setRGB(i, j, 0);
                }
            }
        }

        // 输出图片
        try (FileOutputStream fos = new FileOutputStream("D:/checkCodeResult.jpg")) {
            ImageIO.write(image, "jpg", fos);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 获取像素点
     *
     * @param image 图片对象
     * @param x     像素点所在图片x轴坐标
     * @param y     像素点所在图片y轴坐标
     * @return 像素点对象
     */
    static Pixel getPixel(BufferedImage image, int x, int y) {
        int pixelTop = image.getRGB(x, y);

        int r = (pixelTop & 0xff0000) >> 16;
        int g = (pixelTop & 0xff00) >> 8;
        int b = (pixelTop & 0xff);

        return new Pixel(r, g, b);
    }

    /**
     * 像素点类
     */
    static class Pixel {

        int r;

        int g;

        int b;

        /**
         * 判断像素点是否相似
         *
         * @param pixel 比较像素点
         * @param d     坐标点距离限制
         * @return 是否相似boolean
         */
        public boolean isSimilar(Pixel pixel, int d) {
            return d >= Math.sqrt(Math.pow(r - pixel.r, 2) + Math.pow(g - pixel.g, 2) + Math.pow(b - pixel.b, 2));
        }

        /**
         * 计算坐标中点
         *
         * @param pixel 比较像素点
         * @return 坐标中点
         */
        public Pixel midPixel(Pixel ...pixel) {
            int sr = r;
            int sg = g;
            int sb = b;
            for (Pixel each : pixel) {
                sr += each.r;
                sg += each.g;
                sb += each.b;
            }
            return new Pixel(sr / pixel.length+1, sg / pixel.length+1,  sb/ pixel.length+1);
        }

        /**
         * 获取整形颜色值
         *
         * @return 整形颜色值
         */
        public int getColor() {
            return r << 16 + g << 8 + b;
        }

        public Pixel(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public String toString() {
            return "pixel{" +
                    "r=" + r +
                    ", g=" + g +
                    ", b=" + b +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pixel pixel = (Pixel) o;
            return r == pixel.r && g == pixel.g && b == pixel.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, g, b);
        }
    }

}
