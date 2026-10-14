
package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_especialidades")
public class AuditoriaEspecialidad {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // ESPECIALIDAD AFECTADA
    // ==========================================

    @Column(
            name = "especialidad_id",
            nullable = false
    )
    private Long especialidadId;


    // ==========================================
    // ACCIÓN REALIZADA
    // ==========================================

    @Column(
            name = "accion",
            nullable = false,
            length = 20
    )
    private String accion;


    // ==========================================
    // NOMBRE DE LA ESPECIALIDAD
    // ==========================================

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
    // FECHA ASIGNADA POR POSTGRESQL
    // ==========================================

    @Column(
            name = "fecha_hora",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime fechaHora;


    // ==========================================
    // DESCRIPCIÓN DE LA OPERACIÓN
    // ==========================================

    @Column(
            name = "detalle",
            length = 500
    )
    private String detalle;


    // ==========================================
    // INFORMACIÓN ANTERIOR Y NUEVA
    // ==========================================

    @Column(
            name = "datos_anteriores",
            columnDefinition = "TEXT"
    )
    private String datosAnteriores;


    @Column(
            name = "datos_nuevos",
            columnDefinition = "TEXT"
    )
    private String datosNuevos;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AuditoriaEspecialidad() {
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

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
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

    public String getDatosAnteriores() {
        return datosAnteriores;
    }

    public void setDatosAnteriores(String datosAnteriores) {
        this.datosAnteriores = datosAnteriores;
    }

    public String getDatosNuevos() {
        return datosNuevos;
    }

    public void setDatosNuevos(String datosNuevos) {
        this.datosNuevos = datosNuevos;
    }
}
