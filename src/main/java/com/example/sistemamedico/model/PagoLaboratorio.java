package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "pago_laboratorio")
public class PagoLaboratorio {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // ORDEN DE LABORATORIO
    // =====================================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "orden_id",
            nullable = false,
            unique = true
    )
    private OrdenLaboratorio orden;


    // =====================================================
    // CAJERO QUE REALIZA EL COBRO
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cajero_id",
            nullable = false
    )
    private Usuario cajero;


    // =====================================================
    // NÚMERO DE TRANSACCIÓN
    // =====================================================

    @Column(
            name = "numero_transaccion",
            nullable = false,
            unique = true,
            length = 50
    )
    private String numeroTransaccion;


    // =====================================================
    // MONTO TOTAL PAGADO
    // =====================================================

    @Column(
            name = "monto",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal monto;


    // =====================================================
    // MÉTODO DE PAGO
    // EFECTIVO / VISA / MASTERCARD / DEBITO
    // =====================================================

    @Column(
            name = "metodo_pago",
            nullable = false,
            length = 20
    )
    private String metodoPago;


    // =====================================================
    // EFECTIVO
    // =====================================================

    @Column(
            name = "monto_recibido",
            precision = 10,
            scale = 2
    )
    private BigDecimal montoRecibido;


    @Column(
            name = "cambio",
            precision = 10,
            scale = 2
    )
    private BigDecimal cambio;


    // =====================================================
    // TARJETA
    // =====================================================

    @Column(
            name = "ultimos_cuatro",
            length = 4
    )
    private String ultimosCuatro;


    // =====================================================
    // FECHA
    // =====================================================

    @Column(
            name = "fecha_pago",
            nullable = false
    )
    private LocalDateTime fechaPago;


    public PagoLaboratorio() {
    }


    @PrePersist
    public void prePersist() {

        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public OrdenLaboratorio getOrden() {
        return orden;
    }


    public void setOrden(
            OrdenLaboratorio orden
    ) {
        this.orden = orden;
    }


    public Usuario getCajero() {
        return cajero;
    }


    public void setCajero(
            Usuario cajero
    ) {
        this.cajero = cajero;
    }


    public String getNumeroTransaccion() {
        return numeroTransaccion;
    }


    public void setNumeroTransaccion(
            String numeroTransaccion
    ) {
        this.numeroTransaccion =
                numeroTransaccion;
    }


    public BigDecimal getMonto() {
        return monto;
    }


    public void setMonto(
            BigDecimal monto
    ) {
        this.monto = monto;
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
        this.montoRecibido =
                montoRecibido;
    }


    public BigDecimal getCambio() {
        return cambio;
    }


    public void setCambio(
            BigDecimal cambio
    ) {
        this.cambio = cambio;
    }


    public String getUltimosCuatro() {
        return ultimosCuatro;
    }


    public void setUltimosCuatro(
            String ultimosCuatro
    ) {
        this.ultimosCuatro =
                ultimosCuatro;
    }


    public LocalDateTime getFechaPago() {
        return fechaPago;
    }


    public void setFechaPago(
            LocalDateTime fechaPago
    ) {
        this.fechaPago = fechaPago;
    }
}
