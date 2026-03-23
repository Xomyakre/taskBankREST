package com.example.taskbankrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.example.taskbankrest", "com.example.bankcards"})
@EnableJpaRepositories(basePackages = "com.example.bankcards.repository")
@EntityScan(basePackages = "com.example.bankcards.entity")
public class TaskBankRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskBankRestApplication.class, args);
    }

}