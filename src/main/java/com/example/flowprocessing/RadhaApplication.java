package com.example.flowprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan({"com.example.flowprocessing"})
@EnableScheduling
public class RadhaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RadhaApplication.class, args);
    }

}
