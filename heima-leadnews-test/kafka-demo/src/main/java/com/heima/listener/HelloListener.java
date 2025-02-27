package com.heima.listener;


import com.alibaba.fastjson.JSON;
import com.heima.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HelloListener {

    @KafkaListener(topics = "user-topic")
    public void onMessage(String message) {
        if(!StringUtils.isEmpty(message)) {
            User user = JSON.parseObject(message, User.class);
            System.out.println(user);
        }
    }
}
