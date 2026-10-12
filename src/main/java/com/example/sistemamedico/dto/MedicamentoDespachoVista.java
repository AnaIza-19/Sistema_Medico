package com.example.sistemamedico.dto;

import com.example.sistemamedico.model.RecetaMedicaDetalle;


public class MedicamentoDespachoVista {

    private RecetaMedicaDetalle detalle;

    private Integer stockActual;

    private Integer stockMinimo;

    private boolean inventarioRegistrado;

    private boolean stockBajo;

    private String mensajeInventario;


    public MedicamentoDespachoVista(
            RecetaMedicaDetalle detalle,
            Integer stockActual,
            Integer stockMinimo,
            boolean inventarioRegistrado,
            boolean stockBajo,
            String mensajeInventario
    ) {

        this.detalle = detalle;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.inventarioRegistrado = inventarioRegistrado;
        this.stockBajo = stockBajo;
        this.mensajeInventario = mensajeInventario;
    }


    public RecetaMedicaDetalle getDetalle() {
        return detalle;
    }


    public Integer getStockActual() {
        return stockActual;
    }


    public Integer getStockMinimo() {
        return stockMinimo;
    }


    public boolean isInventarioRegistrado() {
        return inventarioRegistrado;
    }


    public boolean isStockBajo() {
        return stockBajo;
    }


    public String getMensajeInventario() {
        return mensajeInventario;
    }
}