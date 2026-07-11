package com.data.profile.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
        scanBasePackages = {"com.data.profile", "com.data.notification"},
        excludeName = {
                "com.hazelcast.spring.boot.HazelcastAutoConfiguration",
                "com.hazelcast.spring.boot.HazelcastClientAutoConfiguration",
                "org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration"
        }
)
@MapperScan({"com.data.profile.web"})
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableScheduling
public class ProfileWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileWebApplication.class, args);
    }

}
