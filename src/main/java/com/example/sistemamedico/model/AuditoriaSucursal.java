package com.example.sistemamedico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_sucursales")
public class AuditoriaSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "nombre_sucursal", nullable = false, length = 100)
    private String nombreSucursal;

    @Column(name = "ejecutado_por", nullable = false, length = 50)
    private String ejecutadoPor;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "datos_anteriores", columnDefinition = "text")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "text")
    private String datosNuevos;

    public AuditoriaSucursal() { }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getNombreSucursal() { return nombreSucursal; }
    public void setNombreSucursal(String nombreSucursal) { this.nombreSucursal = nombreSucursal; }
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
