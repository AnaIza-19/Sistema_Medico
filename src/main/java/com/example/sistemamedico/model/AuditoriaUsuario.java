package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_usuarios")
public class AuditoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "accion",
            nullable = false,
            length = 30
    )
    private String accion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_afectado_id"
    )
    private Usuario usuarioAfectado;

    @Column(
            name = "nombre_usuario_afectado",
            length = 50
    )
    private String nombreUsuarioAfectado;

    @Column(
            name = "ejecutado_por",
            nullable = false,
            length = 50
    )
    private String ejecutadoPor;

    @Column(
            name = "fecha_hora",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime fechaHora;

    @Column(
            name = "detalle",
            length = 255
    )
    private String detalle;

    public AuditoriaUsuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Usuario getUsuarioAfectado() {
        return usuarioAfectado;
    }

    public void setUsuarioAfectado(Usuario usuarioAfectado) {
        this.usuarioAfectado = usuarioAfectado;
    }

    public String getNombreUsuarioAfectado() {
        return nombreUsuarioAfectado;
    }

    public void setNombreUsuarioAfectado(String nombreUsuarioAfectado) {
        this.nombreUsuarioAfectado = nombreUsuarioAfectado;
    }

    public String getEjecutadoPor() {
        return ejecutadoPor;
    }

    public void setEjecutadoPor(String ejecutadoPor) {
        this.ejecutadoPor = ejecutadoPor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
