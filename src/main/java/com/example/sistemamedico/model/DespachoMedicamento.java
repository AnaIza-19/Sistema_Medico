package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "despacho_medicamento")
public class DespachoMedicamento {


    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // RECETA
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receta_id",
            nullable = false
    )
    private RecetaMedica receta;


    // =====================================================
    // PERSONAL DE FARMACIA
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "farmacia_id",
            nullable = false
    )
    private Usuario farmacia;


    // =====================================================
    // SUCURSAL
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sucursal_id",
            nullable = false
    )
    private Sucursal sucursal;


    // =====================================================
    // FECHA DEL DESPACHO
    // =====================================================

    @Column(
            name = "fecha_despacho",
            nullable = false
    )
    private LocalDateTime fechaDespacho;


    // =====================================================
    // MONTO TOTAL
    // =====================================================

    @Column(
            name = "monto_total",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal montoTotal;


    // =====================================================
    // ESTADO
    // CONFIRMADO / CANCELADO
    // =====================================================

    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private String estado;


    // =====================================================
    // OBSERVACIONES
    // =====================================================

    @Column(
            name = "observaciones",
            columnDefinition = "TEXT"
    )
    private String observaciones;


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public DespachoMedicamento() {
    }


    // =====================================================
    // VALORES POR DEFECTO
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (fechaDespacho == null) {
            fechaDespacho = LocalDateTime.now();
        }

        if (montoTotal == null) {
            montoTotal = BigDecimal.ZERO;
        }

        if (estado == null || estado.isBlank()) {
            estado = "CONFIRMADO";
        }
    }


    // =====================================================
    // GETTERS Y SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public RecetaMedica getReceta() {
        return receta;
    }


    public void setReceta(
            RecetaMedica receta
    ) {
        this.receta = receta;
    }


    public Usuario getFarmacia() {
        return farmacia;
    }


    public void setFarmacia(
            Usuario farmacia
    ) {
        this.farmacia = farmacia;
    }


    public Sucursal getSucursal() {
        return sucursal;
    }


    public void setSucursal(
            Sucursal sucursal
    ) {
        this.sucursal = sucursal;
    }


    public LocalDateTime getFechaDespacho() {
        return fechaDespacho;
    }


    public void setFechaDespacho(
            LocalDateTime fechaDespacho
    ) {
        this.fechaDespacho = fechaDespacho;
    }


    public BigDecimal getMontoTotal() {
        return montoTotal;
    }


    public void setMontoTotal(
            BigDecimal montoTotal
    ) {
        this.montoTotal = montoTotal;
    }


    public String getEstado() {
        return estado;
    }


    public void setEstado(
            String estado
    ) {
        this.estado = estado;
    }


    public String getObservaciones() {
        return observaciones;
    }


    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
}
