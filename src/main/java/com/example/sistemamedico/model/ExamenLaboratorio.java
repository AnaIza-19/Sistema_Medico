package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "examen_laboratorio")
public class ExamenLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 150
    )
    private String nombre;

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;

    @Column(
            name = "precio",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal precio;

    @Column(
            name = "rango_referencia",
            length = 150
    )
    private String rangoReferencia;


    public ExamenLaboratorio() {
    }


    @PrePersist
    public void prePersist() {

        if (activo == null) {
            activo = true;
        }

        if (precio == null) {
            precio = BigDecimal.ZERO;
        }
    }


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


    public String getRangoReferencia() {
        return rangoReferencia;
    }

    public void setRangoReferencia(
            String rangoReferencia
    ) {
        this.rangoReferencia = rangoReferencia;
    }
}