
package com.example.sistemamedico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ==========================================
    // INTERCEPTOR DE SESIÓN ADMINISTRATIVA
    // ==========================================

    private final AdminSessionInterceptor
            adminSessionInterceptor;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public WebConfig(
            AdminSessionInterceptor adminSessionInterceptor
    ) {
        this.adminSessionInterceptor =
                adminSessionInterceptor;
    }

    // ==========================================
    // REGISTRAR INTERCEPTORES
    // ==========================================

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {

        registry.addInterceptor(
                adminSessionInterceptor
        ).addPathPatterns(

                // ==================================
                // CU-01: ADMINISTRACIÓN DE USUARIOS
                // ==================================

                "/admin/usuarios",
                "/admin/usuarios/**",

                // ==================================
                // CU-14: ESPECIALIDADES
                // ==================================

                "/admin/especialidades",
                "/admin/especialidades/**",

                // ==================================
                // CU-14: SUCURSALES
                // ==================================

                "/admin/sucursales",
                "/admin/sucursales/**",

                // ==================================
                // CU-14: ESTADOS DE CITA
                // ==================================

                "/admin/estados-cita",
                "/admin/estados-cita/**",

                // ==================================
                // CU-14: LABORATORIOS
                // ==================================

                "/admin/laboratorios",
                "/admin/laboratorios/**",

                // ==================================
                // CU-14: EXÁMENES DE LABORATORIO
                // ==================================

                "/admin/examenes-laboratorio",
                "/admin/examenes-laboratorio/**",

                // ==================================
                // CU-14: MEDICAMENTOS
                // ==================================

                "/admin/medicamentos",
                "/admin/medicamentos/**",

                // ==================================
                // CU-14: ROLES
                // ==================================

                "/admin/roles",
                "/admin/roles/**",

                // ==================================
                // CU-14: INVENTARIO DE MEDICAMENTOS
                // ==================================

                "/admin/inventario-medicamentos",
                "/admin/inventario-medicamentos/**"

        );
    }
}
