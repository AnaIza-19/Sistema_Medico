
package com.example.sistemamedico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_sucursal_especialidad")
public class AuditoriaSucursalEspecialidad {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // ACCIÓN REALIZADA
    // ASIGNAR / ELIMINAR
    // ==========================================

    @Column(
            name = "accion",
            nullable = false,
            length = 30
    )
    private String accion;


    // ==========================================
    // ID DE LA ASIGNACIÓN
    // ==========================================

    @Column(name = "asignacion_id")
    private Long asignacionId;


    // ==========================================
    // DATOS DE LA SUCURSAL
    // ==========================================

    @Column(
            name = "sucursal_id",
            nullable = false
    )
    private Long sucursalId;

    @Column(
            name = "nombre_sucursal",
            nullable = false,
            length = 100
    )
    private String nombreSucursal;


    // ==========================================
    // DATOS DE LA ESPECIALIDAD
    // ==========================================

    @Column(
            name = "especialidad_id",
            nullable = false
    )
    private Long especialidadId;

    @Column(
            name = "nombre_especialidad",
            nullable = false,
            length = 200
    )
    private String nombreEspecialidad;


    // ==========================================
    // ADMINISTRADOR RESPONSABLE
    // ==========================================

    @Column(
            name = "ejecutado_por",
            nullable = false,
            length = 50
    )
    private String ejecutadoPor;


    // ==========================================
    // FECHA Y HORA
    // ==========================================

    @Column(
            name = "fecha_hora",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaHora;


    // ==========================================
    // DESCRIPCIÓN DE LA OPERACIÓN
    // ==========================================

    @Column(
            name = "detalle",
            length = 255
    )
    private String detalle;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AuditoriaSucursalEspecialidad() {
    }


    // ==========================================
    // ASIGNAR FECHA AUTOMÁTICAMENTE
    // ==========================================

    @PrePersist
    public void antesDeGuardar() {

        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }


    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Long getAsignacionId() {
        return asignacionId;
    }

    public void setAsignacionId(Long asignacionId) {
        this.asignacionId = asignacionId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getNombreSucursal() {
        return nombreSucursal;
    }

    public void setNombreSucursal(String nombreSucursal) {
        this.nombreSucursal = nombreSucursal;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public String getNombreEspecialidad() {
        return nombreEspecialidad;
    }

    public void setNombreEspecialidad(String nombreEspecialidad) {
        this.nombreEspecialidad = nombreEspecialidad;
    }

    public String getEjecutadoPor() {
        return ejecutadoPor;
    }

    public void setEjecutadoPor(String ejecutadoPor) {
        this.ejecutadoPor = ejecutadoPor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}

