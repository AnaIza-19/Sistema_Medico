package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "es_sistema", nullable = false)
    private Boolean esSistema = false;

    public Rol() { }

    // Se conserva porque lo utiliza código anterior del hospital.
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEsSistema() { return esSistema; }
    public void setEsSistema(Boolean esSistema) { this.esSistema = esSistema; }
}
