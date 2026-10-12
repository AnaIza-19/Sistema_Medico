package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;


@Entity
@Table(name = "medicamento")
public class Medicamento {


    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // NOMBRE
    // =====================================================

    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 150
    )
    private String nombre;


    // =====================================================
    // ACTIVO
    // =====================================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;


    // =====================================================
    // PRECIO
    // CU-11 DESPACHO DE MEDICAMENTOS
    // =====================================================

    @Column(
            name = "precio",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precio;


    // =====================================================
    // UNIDAD
    // EJEMPLO: TABLETA, CÁPSULA, INHALADOR
    // =====================================================

    @Column(
            name = "unidad",
            length = 50
    )
    private String unidad;


    // =====================================================
    // STOCK MÍNIMO
    // CU-11 - ALERTAS DE INVENTARIO
    // =====================================================

    @Column(
            name = "stock_minimo",
            nullable = false
    )
    private Integer stockMinimo;


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public Medicamento() {
    }


    // =====================================================
    // VALORES POR DEFECTO
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (activo == null) {
            activo = true;
        }

        if (precio == null) {
            precio = BigDecimal.ZERO;
        }

        if (stockMinimo == null) {
            stockMinimo = 0;
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


    public String getNombre() {
        return nombre;
    }


    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public Boolean getActivo() {
        return activo;
    }


    public void setActivo(Boolean activo) {
        this.activo = activo;
    }


    public BigDecimal getPrecio() {
        return precio;
    }


    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }


    public String getUnidad() {
        return unidad;
    }


    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }


    public Integer getStockMinimo() {
        return stockMinimo;
    }


    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}