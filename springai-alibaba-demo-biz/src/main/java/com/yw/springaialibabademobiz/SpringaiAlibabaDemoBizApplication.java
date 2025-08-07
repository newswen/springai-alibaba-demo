package com.yw.springaialibabademobiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
//@EnableDiscoveryClient
@SpringBootApplication
//@EnableHooyaFlyway(name = "lz")
public class SpringaiAlibabaDemoBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringaiAlibabaDemoBizApplication.class, args);
    }

}
