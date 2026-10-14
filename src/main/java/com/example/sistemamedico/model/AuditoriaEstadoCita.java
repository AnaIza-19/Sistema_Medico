
package com.example.sistemamedico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_estados_cita")
public class AuditoriaEstadoCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estado_cita_id", nullable = false)
    private Long estadoCitaId;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "codigo_estado", nullable = false, length = 30)
    private String codigoEstado;

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
    public Long getEstadoCitaId() { return estadoCitaId; }
    public void setEstadoCitaId(Long estadoCitaId) { this.estadoCitaId = estadoCitaId; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getCodigoEstado() { return codigoEstado; }
    public void setCodigoEstado(String codigoEstado) { this.codigoEstado = codigoEstado; }
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
