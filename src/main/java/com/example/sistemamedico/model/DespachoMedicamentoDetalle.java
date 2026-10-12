package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;


@Entity
@Table(name = "despacho_medicamento_detalle")
public class DespachoMedicamentoDetalle {


    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // DESPACHO
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "despacho_id",
            nullable = false
    )
    private DespachoMedicamento despacho;


    // =====================================================
    // MEDICAMENTO ORIGINAL DE LA RECETA
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_original_id",
            nullable = false
    )
    private Medicamento medicamentoOriginal;


    // =====================================================
    // MEDICAMENTO REALMENTE DESPACHADO
    // PUEDE SER EL MISMO O UNA SUSTITUCIÓN
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_despachado_id",
            nullable = false
    )
    private Medicamento medicamentoDespachado;


    // =====================================================
    // CANTIDAD
    // =====================================================

    @Column(
            name = "cantidad",
            nullable = false
    )
    private Integer cantidad;


    // =====================================================
    // PRECIO UNITARIO
    // =====================================================

    @Column(
            name = "precio_unitario",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precioUnitario;


    // =====================================================
    // SUBTOTAL
    // =====================================================

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal subtotal;


    // =====================================================
    // SUSTITUCIÓN
    // =====================================================

    @Column(
            name = "sustituido",
            nullable = false
    )
    private Boolean sustituido;


    // =====================================================
    // RAZÓN DE SUSTITUCIÓN
    // =====================================================

    @Column(
            name = "razon_sustitucion",
            columnDefinition = "TEXT"
    )
    private String razonSustitucion;


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public DespachoMedicamentoDetalle() {
    }


    // =====================================================
    // VALORES POR DEFECTO
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (cantidad == null) {
            cantidad = 1;
        }

        if (precioUnitario == null) {
            precioUnitario = BigDecimal.ZERO;
        }

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (sustituido == null) {
            sustituido = false;
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


    public DespachoMedicamento getDespacho() {
        return despacho;
    }


    public void setDespacho(
            DespachoMedicamento despacho
    ) {
        this.despacho = despacho;
    }


    public Medicamento getMedicamentoOriginal() {
        return medicamentoOriginal;
    }


    public void setMedicamentoOriginal(
            Medicamento medicamentoOriginal
    ) {
        this.medicamentoOriginal = medicamentoOriginal;
    }


    public Medicamento getMedicamentoDespachado() {
        return medicamentoDespachado;
    }


    public void setMedicamentoDespachado(
            Medicamento medicamentoDespachado
    ) {
        this.medicamentoDespachado = medicamentoDespachado;
    }


    public Integer getCantidad() {
        return cantidad;
    }


    public void setCantidad(
            Integer cantidad
    ) {
        this.cantidad = cantidad;
    }


    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }


    public void setPrecioUnitario(
            BigDecimal precioUnitario
    ) {
        this.precioUnitario = precioUnitario;
    }


    public BigDecimal getSubtotal() {
        return subtotal;
    }


    public void setSubtotal(
            BigDecimal subtotal
    ) {
        this.subtotal = subtotal;
    }


    public Boolean getSustituido() {
        return sustituido;
    }


    public void setSustituido(
            Boolean sustituido
    ) {
        this.sustituido = sustituido;
    }


    public String getRazonSustitucion() {
        return razonSustitucion;
    }


    public void setRazonSustitucion(
            String razonSustitucion
    ) {
        this.razonSustitucion = razonSustitucion;
    }
}