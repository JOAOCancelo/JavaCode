package com.heima.wemedia.config;

import com.baidu.aip.contentcensor.AipContentCensor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 百度云Ai审核
 */
@Configuration
//@ConfigurationProperties(prefix = "baidu")
public class BaiduConfig {

    @Value("${baidu.appId}")
    private String appId;
    @Value("${baidu.apiKey}")
    private String apiKey;
    @Value("${baidu.secretKey}")
    private String secretKey;


    @Bean
    public AipContentCensor aipContentCensor(){

        AipContentCensor aipContentCensor = new AipContentCensor(appId, apiKey, secretKey);

        // 可选：设置网络连接参数
//        aipContentCensor.setConnectionTimeoutInMillis(2000);
//        aipContentCensor.setSocketTimeoutInMillis(60000);

        return aipContentCensor;
    }





}