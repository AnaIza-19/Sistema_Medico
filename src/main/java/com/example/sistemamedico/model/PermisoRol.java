
package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos_rol")
public class PermisoRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(
            name = "codigo",
            nullable = false,
            length = 100
    )
    private String codigo;


    public PermisoRol() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
