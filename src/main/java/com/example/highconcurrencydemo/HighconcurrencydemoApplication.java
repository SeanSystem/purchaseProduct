package com.example.highconcurrencydemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = "com.example.highconcurrencydemo.dao")
@EnableScheduling
public class HighconcurrencydemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighconcurrencydemoApplication.class, args);
    }

}
