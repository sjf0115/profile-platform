package com.data.profile.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.data.profile")
@MapperScan({"com.data.profile.dao"})
public class ProfileWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileWebApplication.class, args);
    }

}
