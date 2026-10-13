
package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.AuditoriaSucursalEspecialidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaSucursalEspecialidad_Repositorio
        extends JpaRepository<AuditoriaSucursalEspecialidad, Long> {

    // ==========================================
    // CU-13
    // CONSULTAR AUDITORÍA POR ADMINISTRADOR
    // ==========================================

    List<AuditoriaSucursalEspecialidad>
    findByEjecutadoPorOrderByFechaHoraDesc(
            String ejecutadoPor
    );


    // ==========================================
    // CONSULTAR AUDITORÍA POR ACCIÓN
    // ASIGNAR / ELIMINAR
    // ==========================================

    List<AuditoriaSucursalEspecialidad>
    findByAccionOrderByFechaHoraDesc(
            String accion
    );


    // ==========================================
    // CONSULTAR HISTORIAL DE UNA ASIGNACIÓN
    // ==========================================

    List<AuditoriaSucursalEspecialidad>
    findByAsignacionIdOrderByFechaHoraDesc(
            Long asignacionId
    );


    // ==========================================
    // CONSULTAR TODAS LAS OPERACIONES
    // ==========================================

    List<AuditoriaSucursalEspecialidad>
    findAllByOrderByFechaHoraDesc();
}
