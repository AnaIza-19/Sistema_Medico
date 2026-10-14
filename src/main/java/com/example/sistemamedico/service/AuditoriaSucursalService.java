package com.example.sistemamedico.service;

import com.example.sistemamedico.model.AuditoriaSucursal;
import com.example.sistemamedico.model.Sucursal;
import com.example.sistemamedico.Repositorio.AuditoriaSucursal_Repositorio;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaSucursalService {
    private final AuditoriaSucursal_Repositorio repositorio;

    public AuditoriaSucursalService(AuditoriaSucursal_Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String capturarDatos(Sucursal s) {
        return "ID: " + s.getId()
                + " | Nombre: " + s.getNombre()
                + " | Teléfono: " + s.getTelefono()
                + " | Dirección: " + s.getDireccion()
                + " | Descripción: " + s.getDescripcion()
                + " | Activo: " + s.getActivo();
    }

    public void registrar(String accion, Sucursal sucursal, String ejecutadoPor,
                          String datosAnteriores, String detalle) {
        AuditoriaSucursal a = new AuditoriaSucursal();
        a.setSucursalId(sucursal.getId());
        a.setAccion(accion);
        a.setNombreSucursal(sucursal.getNombre());
        a.setEjecutadoPor(ejecutadoPor);
        a.setDetalle(detalle);
        a.setDatosAnteriores(datosAnteriores);
        a.setDatosNuevos(capturarDatos(sucursal));
        repositorio.save(a);
    }
}
