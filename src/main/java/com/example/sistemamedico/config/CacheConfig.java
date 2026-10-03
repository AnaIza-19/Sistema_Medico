package com.example.sistemamedico.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        return new ConcurrentMapCacheManager(

                // CU-00
                "servicios",
                "especialidades",
                "ubicaciones",
                "horarios",

                // CU-01
                "rolesAdmin",
                "sucursalesAdmin",
                "especialidadesAdmin",

                // CU-03
                "sucursalesCita",
                "especialidadesPorSucursal"
        );
    }
}