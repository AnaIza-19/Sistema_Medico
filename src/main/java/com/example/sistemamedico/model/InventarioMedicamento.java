
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
    // ESTADO DEL INVENTARIO
    // =====================================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;


    // =====================================================
    // CU-15: CONTROL DE CONCURRENCIA OPTIMISTA
    //
    // Evita que dos transacciones JPA actualicen
    // simultáneamente el mismo inventario
    // utilizando una versión desactualizada.
    //
    // Hibernate administra este valor.
    // No debe incrementarse manualmente.
    // =====================================================

    @Version
    @Column(
            name = "row_version",
            nullable = false
    )
    private Long rowVersion;


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

        // La versión la administra Hibernate
        // mediante @Version.
    }


    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    // -----------------------------------------------------
    // VERIFICAR SI TIENE EXISTENCIAS
    // -----------------------------------------------------

    public boolean tieneStock() {

        return stockActual != null
                && stockActual > 0;
    }


    // -----------------------------------------------------
    // VERIFICAR STOCK BAJO
    //
    // Compara las existencias actuales
    // con el stock mínimo del medicamento.
    // -----------------------------------------------------

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


    // -----------------------------------------------------
    // VERIFICAR SI PUEDE DESPACHAR
    //
    // Evita autorizar cantidades mayores
    // a las existencias disponibles.
    // -----------------------------------------------------

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


    // -----------------------------------------------------
    // ID
    // -----------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // -----------------------------------------------------
    // MEDICAMENTO
    // -----------------------------------------------------

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(
            Medicamento medicamento
    ) {
        this.medicamento = medicamento;
    }


    // -----------------------------------------------------
    // SUCURSAL
    // -----------------------------------------------------

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(
            Sucursal sucursal
    ) {
        this.sucursal = sucursal;
    }


    // -----------------------------------------------------
    // STOCK ACTUAL
    // -----------------------------------------------------

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(
            Integer stockActual
    ) {
        this.stockActual = stockActual;
    }


    // -----------------------------------------------------
    // ACTIVO
    // -----------------------------------------------------

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(
            Boolean activo
    ) {
        this.activo = activo;
    }


    // -----------------------------------------------------
    // CU-15: VERSIÓN DEL INVENTARIO
    //
    // Solo getter. Hibernate administra
    // automáticamente las actualizaciones.
    // -----------------------------------------------------

    public Long getRowVersion() {
        return rowVersion;
    }

}
