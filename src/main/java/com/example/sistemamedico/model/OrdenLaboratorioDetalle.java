package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "orden_laboratorio_detalle",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orden_examen",
                        columnNames = {
                                "orden_id",
                                "examen_id"
                        }
                )
        }
)
public class OrdenLaboratorioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "orden_id",
            nullable = false
    )
    private OrdenLaboratorio orden;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "examen_id",
            nullable = false
    )
    private ExamenLaboratorio examen;


    @Column(
            name = "monto",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal monto;


    @Column(
            name = "valor_resultado",
            length = 255
    )
    private String valorResultado;


    @Column(
            name = "unidad",
            length = 100
    )
    private String unidad;


    @Column(
            name = "fecha_resultado"
    )
    private LocalDateTime fechaResultado;


    @Column(
            name = "fuera_rango",
            nullable = false
    )
    private Boolean fueraRango;


    @Column(
            name = "notas_resultado",
            columnDefinition = "TEXT"
    )
    private String notasResultado;


    @Column(
            name = "publicado",
            nullable = false
    )
    private Boolean publicado;


    public OrdenLaboratorioDetalle() {
    }


    @PrePersist
    public void prePersist() {

        if (monto == null) {
            monto = BigDecimal.ZERO;
        }

        if (fueraRango == null) {
            fueraRango = false;
        }

        if (publicado == null) {
            publicado = false;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public OrdenLaboratorio getOrden() {
        return orden;
    }

    public void setOrden(
            OrdenLaboratorio orden
    ) {
        this.orden = orden;
    }


    public ExamenLaboratorio getExamen() {
        return examen;
    }

    public void setExamen(
            ExamenLaboratorio examen
    ) {
        this.examen = examen;
    }


    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }


    public String getValorResultado() {
        return valorResultado;
    }

    public void setValorResultado(
            String valorResultado
    ) {
        this.valorResultado = valorResultado;
    }


    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }


    public LocalDateTime getFechaResultado() {
        return fechaResultado;
    }

    public void setFechaResultado(
            LocalDateTime fechaResultado
    ) {
        this.fechaResultado = fechaResultado;
    }


    public Boolean getFueraRango() {
        return fueraRango;
    }

    public void setFueraRango(
            Boolean fueraRango
    ) {
        this.fueraRango = fueraRango;
    }


    public String getNotasResultado() {
        return notasResultado;
    }

    public void setNotasResultado(
            String notasResultado
    ) {
        this.notasResultado = notasResultado;
    }


    public Boolean getPublicado() {
        return publicado;
    }

    public void setPublicado(
            Boolean publicado
    ) {
        this.publicado = publicado;
    }
}