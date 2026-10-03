package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "codigo_cie10")
public class CodigoCie10 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "codigo",
            nullable = false,
            unique = true,
            length = 20
    )
    private String codigo;


    @Column(
            name = "descripcion",
            nullable = false,
            length = 255
    )
    private String descripcion;


    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo;


    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
}
