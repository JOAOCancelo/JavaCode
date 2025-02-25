package com.heima.article.listener;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constans.WmNewsMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.xml.bind.util.JAXBSource;
import java.util.Map;


@Component
public class ArticleIsDownListener {

    @Autowired
    private ApArticleConfigService apArticleConfigService;
    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void onMessage(String message) {
        if(StringUtils.isNotBlank(message)){
            Map map = JSON.parseObject(message, Map.class);
            apArticleConfigService.updateByMap(map);
        }
    }


}
