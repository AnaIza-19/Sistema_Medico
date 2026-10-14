package com.example.sistemamedico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_laboratorios")
public class AuditoriaLaboratorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "laboratorio_id", nullable = false)
    private Long laboratorioId;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "nombre_laboratorio", nullable = false, length = 150)
    private String nombreLaboratorio;

    @Column(name = "ejecutado_por", nullable = false, length = 100)
    private String ejecutadoPor;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "TEXT")
    private String datosNuevos;

    public AuditoriaLaboratorio() { }
    public Long getId() { return id; }
    public Long getLaboratorioId() { return laboratorioId; }
    public void setLaboratorioId(Long laboratorioId) { this.laboratorioId = laboratorioId; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getNombreLaboratorio() { return nombreLaboratorio; }
    public void setNombreLaboratorio(String nombreLaboratorio) { this.nombreLaboratorio = nombreLaboratorio; }
    public String getEjecutadoPor() { return ejecutadoPor; }
    public void setEjecutadoPor(String ejecutadoPor) { this.ejecutadoPor = ejecutadoPor; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getDatosAnteriores() { return datosAnteriores; }
    public void setDatosAnteriores(String datosAnteriores) { this.datosAnteriores = datosAnteriores; }
    public String getDatosNuevos() { return datosNuevos; }
    public void setDatosNuevos(String datosNuevos) { this.datosNuevos = datosNuevos; }
}

