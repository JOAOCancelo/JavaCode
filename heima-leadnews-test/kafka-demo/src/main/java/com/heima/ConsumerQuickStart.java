package com.heima;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * 消费者
 */
public class ConsumerQuickStart {
    public static void main(String[] args) {


        //1.kafka的配置信息
        Properties prop = new Properties();
        //链接地址
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.130:9092");
        //key和value的反序列化器
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        //设置消费者组
        prop.put(ConsumerConfig.GROUP_ID_CONFIG,"group1");

        //手动提交偏移量
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        //2.创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
        //3.订阅主题
        consumer.subscribe(Collections.singletonList("topic-first"));
        //4.拉取信息

        //同步提交和异步提交偏移量
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));//频率
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    System.out.println(consumerRecord.key());
                    System.out.println(consumerRecord.value());
                }
                //异步提交偏移量
                consumer.commitAsync();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //异步提交失败 选择他同步提交
            consumer.commitSync();




                //try {
                //    //同步提交偏移量
                //    consumer.commitSync();
                //}catch (CommitFailedException e){
                //    System.out.println("记录提交失败的异常"+e);
                //}

            //异步的方式提交偏移量

            //consumer.commitAsync(new OffsetCommitCallback() {
            //    @Override
            //    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
            //        if(e != null){
            //            System.out.println("记录错误的提交偏移量"+map+"，异常信息为："+e);
            //        }
            //    }
            //});
        }
    }
}
