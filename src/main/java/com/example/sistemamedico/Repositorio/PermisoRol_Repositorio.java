
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.PermisoRol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermisoRol_Repositorio
        extends JpaRepository<PermisoRol, Long> {

    // ==========================================
    // CU-14
    // VERIFICAR PERMISO ESPECÍFICO DEL ROL
    // ==========================================

    boolean existsByRolIdAndCodigo(
            Long rolId,
            String codigo
    );
}
