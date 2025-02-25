package com.heima;


import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * 生产者
 */
public class ProducerQuickStart {
    public static void main(String[] args) {
        //1.kafka链接配置信息
        Properties props = new Properties();
        //kafka链接地址
        props.put("bootstrap.servers", "192.168.200.130:9092");
        //key和value的序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");

        //ack配置 消息确认机制
        props.put(ProducerConfig.ACKS_CONFIG,"all");
        //重试次数
        props.put(ProducerConfig.RETRIES_CONFIG,10);
        //数据压缩
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,"gzip");

        //2.创建kafka生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        //3.发送消息
        /**
         * 第一个参数：topic
         * 第二个参数：消息的key
         * 第三个参数：消息的value
         */
        ProducerRecord<String, String> kvProducerRecord = new ProducerRecord<String, String>("topic-first", "topic-first","Hello Kafka");
        //同步消息发送
        //RecordMetadata recordMetadata = producer.send(kvProducerRecord).get();
        //System.out.println(recordMetadata.offset());

        //异步消息发送
        producer.send(kvProducerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null) {
                    System.out.println("记录异常信息到日志中");
                }
                System.out.println(recordMetadata.offset());//偏移量
            }
        });

        //4.关闭消息通道  必须要关闭 否则消息发送不成功
        producer.close();

    }
}
