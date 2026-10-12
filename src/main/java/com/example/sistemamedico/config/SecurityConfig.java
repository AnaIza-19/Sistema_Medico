package com.example.sistemamedico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http

                // =================================================
                // AUTORIZACIÓN DE RUTAS
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(


                                // =========================
                                // PORTAL PÚBLICO - CU-00
                                // =========================

                                "/",
                                "/index.html",

                                "/css/**",
                                "/js/**",
                                "/images/**",

                                "/api/verificar-dpi",

                                "/login",
                                "/login-paciente",

                                "/dashboard",

                                "/cerrar-sesion",

                                "/registro",


                                // =========================
                                // PORTAL DEL PACIENTE
                                // CU-11 HISTORIAL
                                // =========================

                                "/paciente",
                                "/paciente/**",


                                // =========================
                                // PERSONAL INTERNO - CU-05
                                // =========================

                                "/personal/login",
                                "/personal/**",


                                // =========================
                                // AGENDAMIENTO
                                // CU-03 / CU-04
                                // =========================

                                "/citas/**",


                                // =========================
                                // RECEPCIÓN - CU-05
                                // =========================

                                "/recepcion",
                                "/recepcion/**",


                                // =========================
                                // CAJA - CU-06
                                // =========================

                                "/caja",
                                "/caja/**",


                                // =========================
                                // ENFERMERÍA - CU-07
                                // =========================

                                "/enfermeria",
                                "/enfermeria/**",


                                // =========================
                                // ADMINISTRACIÓN - CU-01
                                // =========================

                                "/admin/**",


                                // =========================
                                // MÉDICO - CU-08
                                // =========================

                                "/medico",
                                "/medico/**",


                                // =========================
                                // LABORATORIO - CU-09
                                // =========================

                                "/laboratorio",
                                "/laboratorio/**",


                                // =========================
                                // FARMACIA - CU-11
                                // =========================

                                "/farmacia",
                                "/farmacia/**",


                                // =========================
                                // ERROR
                                // =========================

                                "/error"

                        ).permitAll()


                        .anyRequest()
                        .authenticated()
                )


                // =================================================
                // LOGIN CONTROLADO MANUALMENTE
                // =================================================

                .formLogin(
                        form ->
                                form.disable()
                )


                // =================================================
                // HTTP BASIC DESACTIVADO
                // =================================================

                .httpBasic(
                        basic ->
                                basic.disable()
                )


                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf -> csrf

                        .ignoringRequestMatchers(


                                "/api/verificar-dpi",

                                "/login-paciente",

                                "/cerrar-sesion",

                                "/admin/**",


                                // =========================
                                // PERSONAL INTERNO
                                // =========================

                                "/personal/**",


                                // =========================
                                // RECEPCIÓN - CU-05
                                // =========================

                                "/recepcion/**",


                                // =========================
                                // CAJA - CU-06
                                // =========================

                                "/caja/**",


                                // =========================
                                // ENFERMERÍA - CU-07
                                // =========================

                                "/enfermeria/**"

                        )
                );


        return http.build();
    }
}