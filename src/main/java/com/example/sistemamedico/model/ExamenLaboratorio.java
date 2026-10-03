package com.example.sistemamedico.model;

import jakarta.persistence.*;

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

    public ExamenLaboratorio() {
    }

    @PrePersist
    public void prePersist() {

        if (activo == null) {
            activo = true;
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
}