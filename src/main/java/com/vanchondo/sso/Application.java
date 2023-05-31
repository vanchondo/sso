package com.vanchondo.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableMongoRepositories
@EnableEncryptableProperties
public class Application {
    
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
