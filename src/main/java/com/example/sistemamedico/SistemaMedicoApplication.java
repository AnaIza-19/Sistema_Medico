package com.example.sistemamedico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class SistemaMedicoApplication {
    public static void main(String[] args) {
        SpringApplication.run(
                SistemaMedicoApplication.class,
                args
        );
    }
}


