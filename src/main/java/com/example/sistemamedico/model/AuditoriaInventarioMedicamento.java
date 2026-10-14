
package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_inventario_medicamento")
public class AuditoriaInventarioMedicamento {

    // ==========================================
    // ID DE AUDITORÍA
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // ID DEL INVENTARIO
    // ==========================================

    @Column(
            name = "inventario_id",
            nullable = false
    )
    private Long inventarioId;


    // ==========================================
    // ID DEL MEDICAMENTO
    // ==========================================

    @Column(
            name = "medicamento_id",
            nullable = false
    )
    private Long medicamentoId;


    // ==========================================
    // ID DE LA SUCURSAL
    // ==========================================

    @Column(
            name = "sucursal_id",
            nullable = false
    )
    private Long sucursalId;


    // ==========================================
    // ACCIÓN REALIZADA
    // CREAR / AJUSTAR_STOCK /
    // DESACTIVAR / REACTIVAR
    // ==========================================

    @Column(
            name = "accion",
            nullable = false,
            length = 30
    )
    private String accion;


    // ==========================================
    // ADMINISTRADOR QUE REALIZÓ LA ACCIÓN
    // ==========================================

    @Column(
            name = "ejecutado_por",
            nullable = false,
            length = 100
    )
    private String ejecutadoPor;


    // ==========================================
    // FECHA Y HORA DE LA AUDITORÍA
    // ASIGNADA POR POSTGRESQL
    // ==========================================

    @Column(
            name = "fecha_hora",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime fechaHora;


    // ==========================================
    // STOCK ANTERIOR
    // ==========================================

    @Column(name = "stock_anterior")
    private Integer stockAnterior;


    // ==========================================
    // STOCK NUEVO
    // ==========================================

    @Column(name = "stock_nuevo")
    private Integer stockNuevo;


    // ==========================================
    // DETALLE DE LA OPERACIÓN
    // ==========================================

    @Column(
            name = "detalle",
            length = 500
    )
    private String detalle;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public AuditoriaInventarioMedicamento() {
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


    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Long inventarioId) {
        this.inventarioId = inventarioId;
    }


    public Long getMedicamentoId() {
        return medicamentoId;
    }

    public void setMedicamentoId(Long medicamentoId) {
        this.medicamentoId = medicamentoId;
    }


    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }


    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
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


    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }


    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }


    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}

