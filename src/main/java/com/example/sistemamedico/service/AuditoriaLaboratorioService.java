package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaLaboratorio_Repositorio;
import com.example.sistemamedico.model.AuditoriaLaboratorio;
import com.example.sistemamedico.model.Laboratorio;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaLaboratorioService {
    private final AuditoriaLaboratorio_Repositorio repositorio;

    public AuditoriaLaboratorioService(AuditoriaLaboratorio_Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String capturarDatos(Laboratorio laboratorio) {
        if (laboratorio == null) return null;
        return "ID: " + laboratorio.getId()
                + " | Nombre: " + laboratorio.getNombre()
                + " | Teléfono: " + laboratorio.getTelefono()
                + " | Dirección: " + laboratorio.getDireccion()
                + " | Descripción: " + laboratorio.getDescripcion()
                + " | Activo: " + laboratorio.getActivo();
    }

    public void registrar(String accion, Laboratorio laboratorio, String actor,
                          String anteriores, String detalle) {
        AuditoriaLaboratorio auditoria = new AuditoriaLaboratorio();
        auditoria.setLaboratorioId(laboratorio.getId());
        auditoria.setAccion(accion);
        auditoria.setNombreLaboratorio(laboratorio.getNombre());
        auditoria.setEjecutadoPor(actor);
        auditoria.setDetalle(detalle);
        auditoria.setDatosAnteriores(anteriores);
        auditoria.setDatosNuevos(capturarDatos(laboratorio));
        repositorio.save(auditoria);
    }
}
