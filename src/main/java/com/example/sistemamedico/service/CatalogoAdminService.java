package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Rol;
import com.example.sistemamedico.model.Sucursal;
import com.example.sistemamedico.model.Especialidad;

import com.example.sistemamedico.Repositorio.Rol_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;
import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoAdminService {

    private final Rol_Repositorio rolRepository;
    private final Sucursal_Repositorio sucursalRepository;
    private final Especialidad_Repositorio especialidadRepository;

    public CatalogoAdminService(
            Rol_Repositorio rolRepository,
            Sucursal_Repositorio sucursalRepository,
            Especialidad_Repositorio especialidadRepository
    ) {
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
        this.especialidadRepository = especialidadRepository;
    }

    @Cacheable("rolesAdmin")
    public List<Rol> obtenerRoles() {

        System.out.println(
                "Cargando roles administrativos desde BD..."
        );

        return rolRepository.findAll();
    }

    @Cacheable("sucursalesAdmin")
    public List<Sucursal> obtenerSucursales() {

        System.out.println(
                "Cargando sucursales administrativas desde BD..."
        );

        return sucursalRepository.findByActivoTrue();
    }

    @Cacheable("especialidadesAdmin")
    public List<Especialidad> obtenerEspecialidades() {

        System.out.println(
                "Cargando especialidades administrativas desde BD..."
        );

        return especialidadRepository.findByActivoTrue();
    }
}