package com.track;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.track.mapper")
public class TrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackApplication.class, args);
    }

}