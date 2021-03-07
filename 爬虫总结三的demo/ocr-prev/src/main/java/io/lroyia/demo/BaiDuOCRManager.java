package io.lroyia.demo;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 百度OCRApi二次封装
 * 为了限制QPS，这里全部方法都加了同步锁
 * @author <a href="https://blog.lroyia.top">lroyia</a>
 * @since 2021/3/7 16:04
 **/
public class BaiDuOCRManager {

    // 当前使用的是个人注册的百度智能云的智能识别应用，如需部署，请使用公司注册的百度智能云秘钥信息
    //你的 App ID
    public static final String APP_ID = "15805634";
    //你的 Api Key
    public static final String API_KEY = "9HotkyxBesB2i5MsFkGPH5N6";
    //你的 Secret Key
    public static final String SECRET_KEY = "kFoZZszooZWCwHyoRYepcv4a3qIwUSBb";

    // 百度OCRSdk的Api客户端类
    private final AipOcr client;

    private BaiDuOCRManager(){
        client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
        //client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
        //client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        //System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
    }

    /**
     * 获取工具实例
     * @return 获取结果
     */
    public static BaiDuOCRManager getInstance() {
        return new BaiDuOCRManager();
    }

    /**
     * 普通图片文字识别（精度低，免费日限50000次调用）
     * @param imageBytes 图片byte数组
     * @param param 额外参数
     * @return 解析结果 返回null：请求api出错；返回空数组：解析不到文字；
     */
    public synchronized String[] baseGeneral(byte[] imageBytes, HashMap<String, String> param){
        JSONObject res = client.basicGeneral(imageBytes, param);
        JSONArray array = res.getJSONArray("words_result");
        if(array == null) return null;
        String[] result = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            result[i] = array.getJSONObject(i).getString("words");
        }
        return result;
    }

    /**
     * 高精度图片文字识别（精度高，免费日限500次调用）
     * @param imageBytes 图片byte数组
     * @param param 额外参数
     * @return 解析结果 返回null：请求api出错；返回空数组：解析不到文字；
     */
    public synchronized String[] basicAccurateGeneral(byte[] imageBytes, HashMap<String, String> param){
        JSONObject res = client.basicAccurateGeneral(imageBytes, param);
        JSONArray array = res.getJSONArray("words_result");
        if(array == null) return null;
        String[] result = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            result[i] = array.getJSONObject(i).getString("words");
        }
        return result;
    }

    /**
     * 网络图片文字识别（免费日限500次调用）
     * @param imageBytes 图片byte数组
     * @param param 额外参数
     * @return 解析结果 返回null：请求api出错；返回空数组：解析不到文字；
     */
    public synchronized String[] webImage(byte[] imageBytes, HashMap<String, String> param){
        JSONObject res = client.webImage(imageBytes, param);
        JSONArray array = res.getJSONArray("words_result");
        if(array == null) return null;
        String[] result = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            result[i] = array.getJSONObject(i).getString("words");
        }
        return result;
    }

    /**
     * 外放OcrApi，用于操作以上封装方法以外的操作情况
     * @return ApiOcr
     */
    public AipOcr getApiClient(){
        return client;
    }
}
