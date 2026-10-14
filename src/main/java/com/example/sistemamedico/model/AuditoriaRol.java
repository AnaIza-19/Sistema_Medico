package com.example.sistemamedico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_roles")
public class AuditoriaRol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "nombre_rol", nullable = false, length = 30)
    private String nombreRol;

    @Column(name = "ejecutado_por", nullable = false, length = 100)
    private String ejecutadoPor;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "TEXT")
    private String datosNuevos;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
    public String getEjecutadoPor() { return ejecutadoPor; }
    public void setEjecutadoPor(String ejecutadoPor) { this.ejecutadoPor = ejecutadoPor; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getDatosAnteriores() { return datosAnteriores; }
    public void setDatosAnteriores(String datosAnteriores) { this.datosAnteriores = datosAnteriores; }
    public String getDatosNuevos() { return datosNuevos; }
    public void setDatosNuevos(String datosNuevos) { this.datosNuevos = datosNuevos; }
}
