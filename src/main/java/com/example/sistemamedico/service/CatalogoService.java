
package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Especialidad;

import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;

@Service
public class CatalogoService {

    // ==========================================
    // REPOSITORIO DE ESPECIALIDADES
    // CU-14
    // ==========================================

    private final Especialidad_Repositorio
            especialidadRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CatalogoService(
            Especialidad_Repositorio especialidadRepository
    ) {

        this.especialidadRepository =
                especialidadRepository;
    }


    // ==========================================
    // CU-00
    // SERVICIOS DEL HOSPITAL
    // SE CONSERVA SU FUNCIONAMIENTO
    // ==========================================

    @Cacheable("servicios")
    public List<String> obtenerServicios() {

        System.out.println(
                "Cargando servicios..."
        );

        return List.of(
                "Consulta Médica",
                "Laboratorio",
                "Emergencias"
        );
    }


    // ==========================================
    // CU-14
    // ESPECIALIDADES DESDE POSTGRESQL
    // SOLO ESPECIALIDADES ACTIVAS
    // ==========================================

    @Cacheable("especialidades")
    public List<String> obtenerEspecialidades() {

        System.out.println(
                "Cargando especialidades desde BD..."
        );

        return especialidadRepository
                .findByActivoTrue()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Especialidad::getId
                        )
                )
                .map(
                        Especialidad::getNombre
                )
                .toList();
    }


    // ==========================================
    // CU-00
    // UBICACIONES DEL HOSPITAL
    // SE CONSERVA SU FUNCIONAMIENTO
    // ==========================================

    @Cacheable("ubicaciones")
    public List<String> obtenerUbicaciones() {

        System.out.println(
                "Cargando ubicaciones..."
        );

        return List.of(
                "Hospital Central",
                "Área de Consulta Externa",
                "Área de Emergencias"
        );
    }


    // ==========================================
    // CU-00
    // HORARIOS DEL HOSPITAL
    // SE CONSERVA SU FUNCIONAMIENTO
    // ==========================================

    @Cacheable("horarios")
    public List<String> obtenerHorarios() {

        System.out.println(
                "Cargando horarios..."
        );

        return List.of(
                "Lunes a viernes: 8:00 AM - 5:00 PM",
                "Emergencias: 24 horas"
        );
    }
}
