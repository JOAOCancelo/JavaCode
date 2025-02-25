package com.heima.search.service.impl;


import com.heima.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;



@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 保存用户搜索历史记录
     * @param keyword
     * @param userId
     */
    @Override
    public void insert(String keyword, Integer userId) {

        //1.查询当前用户的搜索关键词

        //2.存在 更新创建时间
        //3.不存在 判断当前历史记录总数量是否超过10
    }
}
