package com.vanchondo.sso;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@EnableReactiveMongoRepositories
@EnableEncryptableProperties
@EnableCaching
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
