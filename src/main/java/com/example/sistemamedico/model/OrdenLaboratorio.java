package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_laboratorio")
public class OrdenLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medico_id",
            nullable = false
    )
    private Usuario medico;


    @Column(
            name = "observaciones",
            columnDefinition = "TEXT"
    )
    private String observaciones;


    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private String estado;


    @Column(
            name = "monto_total",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal montoTotal;


    @Column(
            name = "orden_externa",
            nullable = false
    )
    private Boolean ordenExterna;


    public OrdenLaboratorio() {
    }


    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }

        if (montoTotal == null) {
            montoTotal = BigDecimal.ZERO;
        }

        if (ordenExterna == null) {
            ordenExterna = false;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }


    public Usuario getMedico() {
        return medico;
    }

    public void setMedico(Usuario medico) {
        this.medico = medico;
    }


    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDateTime fechaCreacion
    ) {
        this.fechaCreacion = fechaCreacion;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(
            BigDecimal montoTotal
    ) {
        this.montoTotal = montoTotal;
    }


    public Boolean getOrdenExterna() {
        return ordenExterna;
    }

    public void setOrdenExterna(
            Boolean ordenExterna
    ) {
        this.ordenExterna = ordenExterna;
    }
}