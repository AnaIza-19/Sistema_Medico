
package com.example.sistemamedico.service;

import com.example.sistemamedico.model.AuditoriaEspecialidad;
import com.example.sistemamedico.model.Especialidad;

import com.example.sistemamedico.Repositorio.AuditoriaEspecialidad_Repositorio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaEspecialidadService {

    // ==========================================
    // REPOSITORIO
    // ==========================================

    private final AuditoriaEspecialidad_Repositorio
            auditoriaRepository;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AuditoriaEspecialidadService(
            AuditoriaEspecialidad_Repositorio auditoriaRepository
    ) {

        this.auditoriaRepository =
                auditoriaRepository;
    }


    // ==========================================
    // OBTENER DATOS DE UNA ESPECIALIDAD
    // PARA CONSERVAR SU HISTORIAL
    // ==========================================

    public String capturarDatos(
            Especialidad especialidad
    ) {

        if (especialidad == null) {

            throw new IllegalArgumentException(
                    "La especialidad es obligatoria."
            );
        }

        return "ID: " + especialidad.getId()
                + " | Nombre: " + especialidad.getNombre()
                + " | Descripción: " + especialidad.getDescripcion()
                + " | Precio: " + especialidad.getPrecio()
                + " | Activo: " + especialidad.getActivo();
    }


    // ==========================================
    // CU-14
    // REGISTRAR CREACIÓN
    // ==========================================

    @Transactional
    public void registrarCreacion(

            Especialidad especialidad,
            String ejecutadoPor

    ) {

        guardarAuditoria(
                "CREAR",
                especialidad,
                ejecutadoPor,
                null
        );
    }


    // ==========================================
    // CU-14
    // REGISTRAR ACTUALIZACIÓN
    // ==========================================

    @Transactional
    public void registrarActualizacion(

            Especialidad especialidad,
            String ejecutadoPor,
            String datosAnteriores

    ) {

        guardarAuditoria(
                "ACTUALIZAR",
                especialidad,
                ejecutadoPor,
                datosAnteriores
        );
    }


    // ==========================================
    // CU-14
    // REGISTRAR DESACTIVACIÓN
    // ==========================================

    @Transactional
    public void registrarDesactivacion(

            Especialidad especialidad,
            String ejecutadoPor,
            String datosAnteriores

    ) {

        guardarAuditoria(
                "DESACTIVAR",
                especialidad,
                ejecutadoPor,
                datosAnteriores
        );
    }


    // ==========================================
    // CU-14
    // REGISTRAR REACTIVACIÓN
    // ==========================================

    @Transactional
    public void registrarReactivacion(

            Especialidad especialidad,
            String ejecutadoPor,
            String datosAnteriores

    ) {

        guardarAuditoria(
                "REACTIVAR",
                especialidad,
                ejecutadoPor,
                datosAnteriores
        );
    }


    // ==========================================
    // MÉTODO INTERNO
    // GUARDAR REGISTRO DE AUDITORÍA
    // ==========================================

    private void guardarAuditoria(

            String accion,
            Especialidad especialidad,
            String ejecutadoPor,
            String datosAnteriores

    ) {

        // VALIDAR ESPECIALIDAD

        if (especialidad == null ||
                especialidad.getId() == null) {

            throw new IllegalArgumentException(
                    "La especialidad debe estar registrada."
            );
        }


        // VALIDAR ADMINISTRADOR

        if (ejecutadoPor == null ||
                ejecutadoPor.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe identificarse el administrador."
            );
        }


        // VALIDAR ACCIÓN

        String detalle = switch (accion) {

            case "CREAR" ->
                    "Se creó la especialidad ";

            case "ACTUALIZAR" ->
                    "Se actualizó la especialidad ";

            case "DESACTIVAR" ->
                    "Se desactivó la especialidad ";

            case "REACTIVAR" ->
                    "Se reactivó la especialidad ";

            default ->
                    throw new IllegalArgumentException(
                            "Acción de auditoría no válida."
                    );
        };


        // CREAR AUDITORÍA

        AuditoriaEspecialidad auditoria =
                new AuditoriaEspecialidad();


        auditoria.setEspecialidadId(
                especialidad.getId()
        );

        auditoria.setAccion(
                accion
        );

        auditoria.setNombreEspecialidad(
                especialidad.getNombre()
        );

        auditoria.setEjecutadoPor(
                ejecutadoPor.trim()
        );


        // DESCRIPCIÓN

        auditoria.setDetalle(
                detalle + especialidad.getNombre()
        );


        // DATOS ANTERIORES

        auditoria.setDatosAnteriores(
                datosAnteriores
        );


        // DATOS NUEVOS

        auditoria.setDatosNuevos(
                capturarDatos(especialidad)
        );


        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        auditoriaRepository.save(
                auditoria
        );
    }


    // ==========================================
    // CONSULTAR TODAS LAS AUDITORÍAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaEspecialidad>
    listarAuditorias() {

        return auditoriaRepository
                .findAllByOrderByFechaHoraDesc();
    }


    // ==========================================
    // CONSULTAR AUDITORÍA POR ESPECIALIDAD
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaEspecialidad>
    buscarPorEspecialidad(Long especialidadId) {

        return auditoriaRepository
                .findByEspecialidadIdOrderByFechaHoraDesc(
                        especialidadId
                );
    }


    // ==========================================
    // CONSULTAR AUDITORÍA POR ADMINISTRADOR
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaEspecialidad>
    buscarPorAdministrador(String ejecutadoPor) {

        return auditoriaRepository
                .findByEjecutadoPorOrderByFechaHoraDesc(
                        ejecutadoPor
                );
    }


    // ==========================================
    // CONSULTAR AUDITORÍA POR ACCIÓN
    // ==========================================

    @Transactional(readOnly = true)
    public List<AuditoriaEspecialidad>
    buscarPorAccion(String accion) {

        return auditoriaRepository
                .findByAccionOrderByFechaHoraDesc(
                        accion
                );
    }
}
