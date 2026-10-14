
package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estados_cita")
public class EstadoCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "codigo",
            nullable = false,
            unique = true,
            length = 30
    )
    private String codigo;

    @Column(
            name = "nombre",
            nullable = false,
            length = 100
    )
    private String nombre;

    @Column(
            name = "descripcion",
            length = 500
    )
    private String descripcion;

    @Column(
            name = "activo",
            nullable = false
    )
    private Boolean activo = true;

    @Column(
            name = "es_sistema",
            nullable = false
    )
    private Boolean esSistema = false;

    public EstadoCita() {
    }

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

    public Boolean getEsSistema() {
        return esSistema;
    }

    public void setEsSistema(Boolean esSistema) {
        this.esSistema = esSistema;
    }
}
