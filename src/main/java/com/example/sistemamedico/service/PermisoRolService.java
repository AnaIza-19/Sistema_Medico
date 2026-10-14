
package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.Repositorio.PermisoRol_Repositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermisoRolService {

    // ==========================================
    // REPOSITORIO DE PERMISOS
    // ==========================================

    private final PermisoRol_Repositorio
            permisoRolRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public PermisoRolService(
            PermisoRol_Repositorio permisoRolRepository
    ) {

        this.permisoRolRepository =
                permisoRolRepository;
    }


    // ==========================================
    // CU-14
    // COMPROBAR PERMISO POR OPERACIÓN
    // ==========================================

    @Transactional(readOnly = true)
    public boolean tienePermiso(
            Usuario usuario,
            String codigo
    ) {

        // Verificar usuario válido

        if (usuario == null) {
            return false;
        }

        // Verificar usuario activo

        if (!Boolean.TRUE.equals(
                usuario.getActivo()
        )) {
            return false;
        }

        // Verificar rol

        if (usuario.getRol() == null ||
                usuario.getRol().getId() == null) {

            return false;
        }

        // Verificar código del permiso

        if (codigo == null ||
                codigo.isBlank()) {

            return false;
        }

        // Consultar permiso en PostgreSQL

        return permisoRolRepository
                .existsByRolIdAndCodigo(
                        usuario.getRol().getId(),
                        codigo.trim()
                );
    }
}
