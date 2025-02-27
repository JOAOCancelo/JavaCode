package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.heima.common.constans.ArticleConstants;
import com.heima.model.search.vos.SearchArticleVo;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @KafkaListener(topics = ArticleConstants.APARTICLE_ES_SYNC_TOPIC)
    public void onMessage(String message) {
        if(StringUtils.isNotBlank(message)){
            SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);//解析message
            IndexRequest indexRequest = new IndexRequest("app_info_article");
            indexRequest.id(searchArticleVo.getId().toString());
            indexRequest.source(message, XContentType.JSON);
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
