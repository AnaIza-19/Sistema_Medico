package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    @Column(
            name = "numero_transaccion",
            nullable = false,
            unique = true,
            length = 100
    )
    private String numeroTransaccion;


    @Column(
            name = "monto",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal monto;
// =====================================================
// MÉTODO DE PAGO
// CU-06
// =====================================================

    @Column(
            name = "metodo_pago",
            length = 30
    )
    private String metodoPago;


// =====================================================
// MONTO RECIBIDO
// SOLO PARA EFECTIVO
// =====================================================

    @Column(
            name = "monto_recibido",
            precision = 10,
            scale = 2
    )
    private BigDecimal montoRecibido;


// =====================================================
// CAMBIO
// SOLO PARA EFECTIVO
// =====================================================

    @Column(
            name = "cambio",
            precision = 10,
            scale = 2
    )
    private BigDecimal cambio;

    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private String estado;


    @Column(
            name = "ultimos_cuatro",
            length = 4
    )
    private String ultimosCuatro;


    @Column(
            name = "marca_tarjeta",
            length = 30
    )
    private String marcaTarjeta;


    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String idempotencyKey;


    @Column(
            name = "fecha_creacion",
            nullable = false
    )
    private LocalDateTime fechaCreacion;


    @Column(
            name = "fecha_pago"
    )
    private LocalDateTime fechaPago;


    public Pago() {
    }


    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }


    public String getNumeroTransaccion() {
        return numeroTransaccion;
    }

    public void setNumeroTransaccion(String numeroTransaccion) {
        this.numeroTransaccion = numeroTransaccion;
    }


    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    public String getUltimosCuatro() {
        return ultimosCuatro;
    }

    public void setUltimosCuatro(String ultimosCuatro) {
        this.ultimosCuatro = ultimosCuatro;
    }


    public String getMarcaTarjeta() {
        return marcaTarjeta;
    }

    public void setMarcaTarjeta(String marcaTarjeta) {
        this.marcaTarjeta = marcaTarjeta;
    }


    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(
            String metodoPago
    ) {
        this.metodoPago = metodoPago;
    }


    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(
            BigDecimal montoRecibido
    ) {
        this.montoRecibido = montoRecibido;
    }


    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(
            BigDecimal cambio
    ) {
        this.cambio = cambio;
    }
}