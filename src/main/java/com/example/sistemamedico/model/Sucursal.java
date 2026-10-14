
package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sucursales")
public class Sucursal {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // NOMBRE
    // ==========================================

    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 100
    )
    private String nombre;


    // ==========================================
    // TELÉFONO
    // ==========================================

    @Column(
            name = "telefono",
            length = 8
    )
    private String telefono;


    // ==========================================
    // DIRECCIÓN
    // ==========================================

    @Column(
            name = "direccion",
            length = 255
    )
    private String direccion;


    // ==========================================
    // DESCRIPCIÓN
    // ==========================================

    @Column(
            name = "descripcion",
            length = 500
    )
    private String descripcion;


    // ==========================================
    // ESTADO
    // ==========================================

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Sucursal() {
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


    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }


    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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
