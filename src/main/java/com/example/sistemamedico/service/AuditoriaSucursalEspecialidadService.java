
package com.example.sistemamedico.service;

import com.example.sistemamedico.model.AuditoriaSucursalEspecialidad;
import com.example.sistemamedico.model.SucursalEspecialidad;

import com.example.sistemamedico.Repositorio.AuditoriaSucursalEspecialidad_Repositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaSucursalEspecialidadService {

    // ==========================================
    // REPOSITORIO
    // ==========================================

    private final AuditoriaSucursalEspecialidad_Repositorio
            auditoriaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AuditoriaSucursalEspecialidadService(
            AuditoriaSucursalEspecialidad_Repositorio
                    auditoriaRepository
    ) {

        this.auditoriaRepository =
                auditoriaRepository;
    }


    // ==========================================
    // CU-13
    // REGISTRAR ASIGNACIÓN DE ESPECIALIDAD
    // ==========================================

    @Transactional
    public void registrarAsignacion(

            SucursalEspecialidad asignacion,

            String ejecutadoPor

    ) {

        String detalle =
                "Se asignó la especialidad "
                        + asignacion.getEspecialidad().getNombre()
                        + " a la sede "
                        + asignacion.getSucursal().getNombre();


        guardarAuditoria(
                "ASIGNAR",
                asignacion,
                ejecutadoPor,
                detalle
        );
    }


    // ==========================================
    // CU-13
    // REGISTRAR ELIMINACIÓN DE ASIGNACIÓN
    // ==========================================

    @Transactional
    public void registrarEliminacion(

            SucursalEspecialidad asignacion,

            String ejecutadoPor

    ) {

        String detalle =
                "Se eliminó la asignación de "
                        + asignacion.getEspecialidad().getNombre()
                        + " en la sede "
                        + asignacion.getSucursal().getNombre();


        guardarAuditoria(
                "ELIMINAR",
                asignacion,
                ejecutadoPor,
                detalle
        );
    }


    // ==========================================
    // MÉTODO INTERNO
    // GUARDAR REGISTRO DE AUDITORÍA
    // ==========================================

    private void guardarAuditoria(

            String accion,

            SucursalEspecialidad asignacion,

            String ejecutadoPor,

            String detalle

    ) {

        if (ejecutadoPor == null ||
                ejecutadoPor.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe identificarse el administrador."
            );
        }


        AuditoriaSucursalEspecialidad auditoria =
                new AuditoriaSucursalEspecialidad();


        // Acción realizada
        auditoria.setAccion(
                accion
        );


        // Identificador de la asignación
        auditoria.setAsignacionId(
                asignacion.getId()
        );


        // Información de la sede
        auditoria.setSucursalId(
                asignacion.getSucursal().getId()
        );

        auditoria.setNombreSucursal(
                asignacion.getSucursal().getNombre()
        );


        // Información de la especialidad
        auditoria.setEspecialidadId(
                asignacion.getEspecialidad().getId()
        );

        auditoria.setNombreEspecialidad(
                asignacion.getEspecialidad().getNombre()
        );


        // Administrador responsable
        auditoria.setEjecutadoPor(
                ejecutadoPor
        );


        // Descripción de la operación
        auditoria.setDetalle(
                detalle
        );


        // Fecha y hora automática:
        // se asigna mediante @PrePersist
        // en AuditoriaSucursalEspecialidad


        // Guardar auditoría en PostgreSQL
        auditoriaRepository.save(
                auditoria
        );
    }


    // ==========================================
    // CONSULTAR AUDITORÍA COMPLETA
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaSucursalEspecialidad>
    listarAuditorias() {

        return auditoriaRepository
                .findAllByOrderByFechaHoraDesc();
    }


    // ==========================================
    // CONSULTAR POR ADMINISTRADOR
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaSucursalEspecialidad>
    buscarPorAdministrador(
            String ejecutadoPor
    ) {

        return auditoriaRepository
                .findByEjecutadoPorOrderByFechaHoraDesc(
                        ejecutadoPor
                );
    }


    // ==========================================
    // CONSULTAR POR ACCIÓN
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaSucursalEspecialidad>
    buscarPorAccion(
            String accion
    ) {

        return auditoriaRepository
                .findByAccionOrderByFechaHoraDesc(
                        accion
                );
    }


    // ==========================================
    // CONSULTAR HISTORIAL POR ASIGNACIÓN
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaSucursalEspecialidad>
    buscarPorAsignacion(
            Long asignacionId
    ) {

        return auditoriaRepository
                .findByAsignacionIdOrderByFechaHoraDesc(
                        asignacionId
                );
    }
}
