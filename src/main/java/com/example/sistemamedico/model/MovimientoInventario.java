
package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    // ==========================================
    // IDENTIFICADOR
    // ==========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // RELACIÓN CON INVENTARIO
    // ==========================================

    @Column(name = "inventario_id", nullable = false)
    private Long inventarioId;


    // ==========================================
    // MEDICAMENTO
    // ==========================================

    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;


    // ==========================================
    // SUCURSAL
    // ==========================================

    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;


    // ==========================================
    // TIPO DE MOVIMIENTO
    //
    // 0 = COMPRA
    // 1 = DEVOLUCION
    // 2 = VENTA
    // 3 = RECLAMO
    // 4 = AJUSTE POSITIVO
    // 5 = AJUSTE NEGATIVO
    // 6 = DESPACHO AUTOMATICO
    // ==========================================

    @Column(name = "tipo_movimiento", nullable = false)
    private Short tipoMovimiento;


    // ==========================================
    // CANTIDAD
    // ==========================================

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;


    // ==========================================
    // EXISTENCIAS
    // ==========================================

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;


    // ==========================================
    // COSTO UNITARIO
    // ==========================================

    @Column(
            name = "costo_unitario",
            precision = 12,
            scale = 2
    )
    private BigDecimal costoUnitario;


    // ==========================================
    // NÚMERO DE REFERENCIA
    // ==========================================

    @Column(name = "numero_referencia", length = 120)
    private String numeroReferencia;


    // ==========================================
    // MOTIVO O JUSTIFICACIÓN
    // ==========================================

    @Column(name = "motivo", length = 500)
    private String motivo;


    // ==========================================
    // USUARIO RESPONSABLE
    // ==========================================

    @Column(
            name = "registrado_por",
            nullable = false,
            length = 100
    )
    private String registradoPor;


    // ==========================================
    // FECHA Y HORA
    // ==========================================

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;


    // ==========================================
    // ESTADO DEL MOVIMIENTO
    // ==========================================

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;


    // ==========================================
    // DETALLE DEL DESPACHO
    // NULL PARA MOVIMIENTOS MANUALES
    // ==========================================

    @Column(name = "despacho_detalle_id", unique = true)
    private Long despachoDetalleId;


    // ==========================================
    // CONSTRUCTOR VACÍO
    // ==========================================

    public MovimientoInventario() {
    }


    // ==========================================
    // VALORES AUTOMÁTICOS
    // ==========================================

    @PrePersist
    public void antesDeGuardar() {

        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }

        if (activo == null) {
            activo = true;
        }
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


    public Short getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(Short tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }


    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
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


    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }


    public String getNumeroReferencia() {
        return numeroReferencia;
    }

    public void setNumeroReferencia(String numeroReferencia) {
        this.numeroReferencia = numeroReferencia;
    }


    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }


    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }


    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }


    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }


    public Long getDespachoDetalleId() {
        return despachoDetalleId;
    }

    public void setDespachoDetalleId(Long despachoDetalleId) {
        this.despachoDetalleId = despachoDetalleId;
    }
}
