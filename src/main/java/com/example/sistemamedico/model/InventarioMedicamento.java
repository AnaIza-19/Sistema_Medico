package com.example.sistemamedico.model;

import jakarta.persistence.*;


@Entity
@Table(
        name = "inventario_medicamento",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_inventario_medicamento_sucursal",
                        columnNames = {
                                "medicamento_id",
                                "sucursal_id"
                        }
                )
        }
)
public class InventarioMedicamento {


    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // MEDICAMENTO
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "medicamento_id",
            nullable = false
    )
    private Medicamento medicamento;


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
    // STOCK ACTUAL
    // =====================================================

    @Column(
            name = "stock_actual",
            nullable = false
    )
    private Integer stockActual;


    // =====================================================
    // ACTIVO
    // =====================================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public InventarioMedicamento() {
    }


    // =====================================================
    // VALORES POR DEFECTO
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (stockActual == null) {
            stockActual = 0;
        }

        if (activo == null) {
            activo = true;
        }
    }


    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    public boolean tieneStock() {

        return stockActual != null
                && stockActual > 0;
    }


    public boolean tieneStockBajo() {

        if (
                medicamento == null
                        ||
                        medicamento.getStockMinimo() == null
                        ||
                        stockActual == null
        ) {

            return false;
        }

        return stockActual
                <= medicamento.getStockMinimo();
    }


    public boolean puedeDespachar(
            Integer cantidad
    ) {

        return cantidad != null
                && cantidad > 0
                && stockActual != null
                && stockActual >= cantidad;
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


    public Medicamento getMedicamento() {
        return medicamento;
    }


    public void setMedicamento(
            Medicamento medicamento
    ) {
        this.medicamento = medicamento;
    }


    public Sucursal getSucursal() {
        return sucursal;
    }


    public void setSucursal(
            Sucursal sucursal
    ) {
        this.sucursal = sucursal;
    }


    public Integer getStockActual() {
        return stockActual;
    }


    public void setStockActual(
            Integer stockActual
    ) {
        this.stockActual = stockActual;
    }


    public Boolean getActivo() {
        return activo;
    }


    public void setActivo(
            Boolean activo
    ) {
        this.activo = activo;
    }
}