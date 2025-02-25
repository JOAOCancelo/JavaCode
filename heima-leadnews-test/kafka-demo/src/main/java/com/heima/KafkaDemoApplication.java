package com.heima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationExtensionsKt;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaDemoApplication.class,args);
    }
}