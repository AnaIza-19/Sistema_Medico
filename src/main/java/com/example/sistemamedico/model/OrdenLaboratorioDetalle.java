package com.example.sistemamedico.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "orden_laboratorio_detalle",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orden_examen",
                        columnNames = {
                                "orden_id",
                                "examen_id"
                        }
                )
        }
)
public class OrdenLaboratorioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ==========================================
    // ORDEN DE LABORATORIO
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "orden_id",
            nullable = false
    )
    private OrdenLaboratorio orden;


    // ==========================================
    // EXAMEN
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "examen_id",
            nullable = false
    )
    private ExamenLaboratorio examen;


    public OrdenLaboratorioDetalle() {
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

    public ExamenLaboratorio getExamen() {
        return examen;
    }

    public void setExamen(
            ExamenLaboratorio examen
    ) {
        this.examen = examen;
    }
}