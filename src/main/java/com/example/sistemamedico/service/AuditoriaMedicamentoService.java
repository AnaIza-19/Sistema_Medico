package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaMedicamento_Repositorio;
import com.example.sistemamedico.model.AuditoriaMedicamento;
import com.example.sistemamedico.model.Medicamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaMedicamentoService {

    private final AuditoriaMedicamento_Repositorio repositorio;

    public AuditoriaMedicamentoService(AuditoriaMedicamento_Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    public String capturarDatos(Medicamento m) {
        if (m == null) return null;
        return "ID: " + m.getId()
                + " | Nombre: " + m.getNombre()
                + " | Precio: " + m.getPrecio()
                + " | Unidad: " + m.getUnidad()
                + " | Stock mínimo: " + m.getStockMinimo()
                + " | Activo: " + m.getActivo();
    }

    @Transactional
    public void registrar(String accion, Medicamento medicamento,
                          String ejecutadoPor, String datosAnteriores, String detalle) {
        AuditoriaMedicamento a = new AuditoriaMedicamento();
        a.setMedicamentoId(medicamento.getId());
        a.setAccion(accion);
        a.setNombreMedicamento(medicamento.getNombre());
        a.setEjecutadoPor(ejecutadoPor);
        a.setDetalle(detalle);
        a.setDatosAnteriores(datosAnteriores);
        a.setDatosNuevos(capturarDatos(medicamento));
        repositorio.save(a);
    }
}
