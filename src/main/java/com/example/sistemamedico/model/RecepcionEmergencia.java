package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recepciones_emergencia")
public class RecepcionEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // PACIENTE
    // =====================================================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "paciente_id",
            nullable = false
    )
    private Usuario paciente;


    // =====================================================
    // PRIORIDAD
    // =====================================================

    @Column(
            name = "prioridad",
            nullable = false,
            length = 20
    )
    private String prioridad;


    // =====================================================
    // HORA DE LLEGADA
    // =====================================================

    @Column(
            name = "hora_llegada",
            nullable = false
    )
    private LocalDateTime horaLlegada;


    // =====================================================
    // ESTADO
    // =====================================================

    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado;


    // =====================================================
    // FECHA DE CREACIÓN
    // =====================================================

    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (prioridad == null) {

            prioridad = "EMERGENCIA";
        }

        if (horaLlegada == null) {

            horaLlegada = LocalDateTime.now();
        }

        if (estado == null) {

            estado = "PENDIENTE_SIGNOS_VITALES";
        }

        if (fechaCreacion == null) {

            fechaCreacion = LocalDateTime.now();
        }
    }


    public RecepcionEmergencia() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Usuario getPaciente() {
        return paciente;
    }


    public void setPaciente(
            Usuario paciente
    ) {
        this.paciente = paciente;
    }


    public String getPrioridad() {
        return prioridad;
    }


    public void setPrioridad(
            String prioridad
    ) {
        this.prioridad = prioridad;
    }


    public LocalDateTime getHoraLlegada() {
        return horaLlegada;
    }


    public void setHoraLlegada(
            LocalDateTime horaLlegada
    ) {
        this.horaLlegada = horaLlegada;
    }


    public String getEstado() {
        return estado;
    }


    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }


    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {
        this.fechaCreacion = fechaCreacion;
    }
}