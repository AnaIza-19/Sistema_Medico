package com.example.sistemamedico.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "signos_vitales")
public class SignoVital {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // CITA
    // =====================================================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "cita_id",
            nullable = false
    )
    private Cita cita;


    // =====================================================
    // ENFERMERO
    // =====================================================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "enfermero_id",
            nullable = false
    )
    private Usuario enfermero;


    // =====================================================
    // PRESIÓN SISTÓLICA
    // Rango de captura CU-07:
    // 60 - 250 mmHg
    // =====================================================

    @Column(
            name = "presion_sistolica",
            nullable = false
    )
    private Integer presionSistolica;


    // =====================================================
    // PRESIÓN DIASTÓLICA
    // Rango de captura CU-07:
    // 40 - 150 mmHg
    // =====================================================

    @Column(
            name = "presion_diastolica",
            nullable = false
    )
    private Integer presionDiastolica;


    // =====================================================
    // TEMPERATURA
    // Rango de captura CU-07:
    // 34 - 42 °C
    // =====================================================

    @Column(
            name = "temperatura",
            nullable = false,
            precision = 4,
            scale = 1
    )
    private BigDecimal temperatura;


    // =====================================================
    // PESO
    // Rango de captura CU-07:
    // 0.5 - 300 kg
    // =====================================================

    @Column(
            name = "peso",
            nullable = false,
            precision = 6,
            scale = 2
    )
    private BigDecimal peso;


    // =====================================================
    // TALLA
    // Rango de captura CU-07:
    // 30 - 250 cm
    // =====================================================

    @Column(
            name = "talla",
            nullable = false,
            precision = 6,
            scale = 2
    )
    private BigDecimal talla;


    // =====================================================
    // FRECUENCIA CARDÍACA
    // Rango de captura CU-07:
    // 30 - 220 lpm
    // =====================================================

    @Column(
            name = "frecuencia_cardiaca",
            nullable = false
    )
    private Integer frecuenciaCardiaca;


    // =====================================================
    // EMERGENCIA
    // CU-07 FA01
    // =====================================================

    @Column(
            name = "es_emergencia",
            nullable = false
    )
    private Boolean esEmergencia = false;


    // =====================================================
    // ALERTA CLÍNICA
    // CU-07 FA03
    // =====================================================

    @Column(
            name = "tiene_alerta_clinica",
            nullable = false
    )
    private Boolean tieneAlertaClinica = false;


    // =====================================================
    // DETALLE DE ALERTA
    // =====================================================

    @Column(
            name = "detalle_alerta",
            columnDefinition = "TEXT"
    )
    private String detalleAlerta;


    // =====================================================
    // FECHA DE REGISTRO
    // =====================================================

    @Column(
            name = "fecha_registro",
            nullable = false
    )
    private LocalDateTime fechaRegistro;


    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    public void prePersist() {

        if (fechaRegistro == null) {

            fechaRegistro =
                    LocalDateTime.now();
        }

        if (esEmergencia == null) {

            esEmergencia =
                    false;
        }

        if (tieneAlertaClinica == null) {

            tieneAlertaClinica =
                    false;
        }
    }


    // =====================================================
    // CONSTRUCTOR VACÍO
    // =====================================================

    public SignoVital() {
    }


    // =====================================================
    // GETTERS Y SETTERS
    // =====================================================

    public Long getId() {

        return id;
    }


    public void setId(
            Long id
    ) {

        this.id =
                id;
    }


    public Cita getCita() {

        return cita;
    }


    public void setCita(
            Cita cita
    ) {

        this.cita =
                cita;
    }


    public Usuario getEnfermero() {

        return enfermero;
    }


    public void setEnfermero(
            Usuario enfermero
    ) {

        this.enfermero =
                enfermero;
    }


    public Integer getPresionSistolica() {

        return presionSistolica;
    }


    public void setPresionSistolica(
            Integer presionSistolica
    ) {

        this.presionSistolica =
                presionSistolica;
    }


    public Integer getPresionDiastolica() {

        return presionDiastolica;
    }


    public void setPresionDiastolica(
            Integer presionDiastolica
    ) {

        this.presionDiastolica =
                presionDiastolica;
    }


    public BigDecimal getTemperatura() {

        return temperatura;
    }


    public void setTemperatura(
            BigDecimal temperatura
    ) {

        this.temperatura =
                temperatura;
    }


    public BigDecimal getPeso() {

        return peso;
    }


    public void setPeso(
            BigDecimal peso
    ) {

        this.peso =
                peso;
    }


    public BigDecimal getTalla() {

        return talla;
    }


    public void setTalla(
            BigDecimal talla
    ) {

        this.talla =
                talla;
    }


    public Integer getFrecuenciaCardiaca() {

        return frecuenciaCardiaca;
    }


    public void setFrecuenciaCardiaca(
            Integer frecuenciaCardiaca
    ) {

        this.frecuenciaCardiaca =
                frecuenciaCardiaca;
    }


    public Boolean getEsEmergencia() {

        return esEmergencia;
    }


    public void setEsEmergencia(
            Boolean esEmergencia
    ) {

        this.esEmergencia =
                esEmergencia;
    }


    public Boolean getTieneAlertaClinica() {

        return tieneAlertaClinica;
    }


    public void setTieneAlertaClinica(
            Boolean tieneAlertaClinica
    ) {

        this.tieneAlertaClinica =
                tieneAlertaClinica;
    }


    public String getDetalleAlerta() {

        return detalleAlerta;
    }


    public void setDetalleAlerta(
            String detalleAlerta
    ) {

        this.detalleAlerta =
                detalleAlerta;
    }


    public LocalDateTime getFechaRegistro() {

        return fechaRegistro;
    }


    public void setFechaRegistro(
            LocalDateTime fechaRegistro
    ) {

        this.fechaRegistro =
                fechaRegistro;
    }
}
