package com.example.sistemamedico.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoService {

    @Cacheable("servicios")
    public List<String> obtenerServicios() {
        System.out.println("Cargando servicios...");
        return List.of(
                "Consulta Médica",
                "Laboratorio",
                "Emergencias"
        );
    }

    @Cacheable("especialidades")
    public List<String> obtenerEspecialidades() {
        System.out.println("Cargando especialidades...");
        return List.of(
                "Medicina General",
                "Pediatría",
                "Cardiología"
        );
    }

    @Cacheable("ubicaciones")
    public List<String> obtenerUbicaciones() {
        System.out.println("Cargando ubicaciones...");
        return List.of(
                "Hospital Central",
                "Área de Consulta Externa",
                "Área de Emergencias"
        );
    }

    @Cacheable("horarios")
    public List<String> obtenerHorarios() {
        System.out.println("Cargando horarios...");
        return List.of(
                "Lunes a viernes: 8:00 AM - 5:00 PM",
                "Emergencias: 24 horas"
        );
    }
}