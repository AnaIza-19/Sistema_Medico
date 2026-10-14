
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaEspecialidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaEspecialidad_Repositorio
        extends JpaRepository<AuditoriaEspecialidad, Long> {

    // ==========================================
    // LISTAR TODAS LAS AUDITORÍAS
    // ==========================================

    List<AuditoriaEspecialidad>
    findAllByOrderByFechaHoraDesc();


    // ==========================================
    // BUSCAR HISTORIAL DE UNA ESPECIALIDAD
    // ==========================================

    List<AuditoriaEspecialidad>
    findByEspecialidadIdOrderByFechaHoraDesc(
            Long especialidadId
    );


    // ==========================================
    // BUSCAR AUDITORÍAS POR ADMINISTRADOR
    // ==========================================

    List<AuditoriaEspecialidad>
    findByEjecutadoPorOrderByFechaHoraDesc(
            String ejecutadoPor
    );


    // ==========================================
    // BUSCAR AUDITORÍAS POR ACCIÓN
    // ==========================================

    List<AuditoriaEspecialidad>
    findByAccionOrderByFechaHoraDesc(
            String accion
    );
}
