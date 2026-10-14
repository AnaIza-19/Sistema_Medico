
package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "especialidades")
public class Especialidad {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // NOMBRE DE LA ESPECIALIDAD
    // CU-14: MÁXIMO 200 CARACTERES
    // ==========================================


    @Column(
            name = "nombre",
            nullable = false,
            length = 200
    )
    private String nombre;



    // ==========================================
    // DESCRIPCIÓN
    // CU-14: MÁXIMO 500 CARACTERES
    // ==========================================

    @Column(
            name = "descripcion",
            length = 500
    )
    private String descripcion;


    // ==========================================
    // ESTADO ACTIVO / INACTIVO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // PRECIO DE CONSULTA
    // CONSERVADO PARA CU-03 Y CU-06
    // ==========================================

    @Column(
            name = "precio",
            precision = 10,
            scale = 2
    )
    private BigDecimal precio;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Especialidad() {
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


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
}
