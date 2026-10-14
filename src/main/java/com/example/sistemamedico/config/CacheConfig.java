
package com.example.sistemamedico.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    // ==========================================
    // CONFIGURACIÓN GENERAL DE CACHÉ
    // ==========================================

    @Bean
    public CacheManager cacheManager() {

        return new ConcurrentMapCacheManager(

                // ==================================
                // CU-00: PORTAL PRINCIPAL
                // ==================================

                "servicios",
                "especialidades",
                "ubicaciones",
                "horarios",

                // ==================================
                // CU-01: ADMINISTRACIÓN
                // ==================================

                "rolesAdmin",
                "sucursalesAdmin",
                "especialidadesAdmin",

                // ==================================
                // CU-03: AGENDAMIENTO
                // ==================================

                "sucursalesCita",
                "especialidadesPorSucursal",

                // ==================================
                // CU-14: ESTADOS DE CITA
                // ==================================

                "estadosCita",

                // ==================================
                // CU-14: LABORATORIOS
                // ==================================

                "laboratoriosAdmin",

                // ==================================
                // CU-14: EXÁMENES DE LABORATORIO
                // ==================================

                "examenesLaboratorio",

                // ==================================
                // CU-14: MEDICAMENTOS
                // ==================================

                "medicamentosAdmin",

                // ==================================
                // CU-14: INVENTARIO DE MEDICAMENTOS
                // ==================================

                "inventariosAdmin"

        );
    }
}
