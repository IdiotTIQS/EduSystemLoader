package com.tiqs.edusystemloader;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.tiqs.mapper")
@SpringBootApplication(scanBasePackages = "com.tiqs")
public class EduSystemLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduSystemLoaderApplication.class, args);
    }

}
