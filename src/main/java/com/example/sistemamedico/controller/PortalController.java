package com.example.sistemamedico.controller;

import com.example.sistemamedico.service.CatalogoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    private final CatalogoService catalogoService;

    public PortalController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/")
    public String mostrarPortal(Model model) {

        model.addAttribute(
                "servicios",
                catalogoService.obtenerServicios()
        );

        model.addAttribute(
                "especialidades",
                catalogoService.obtenerEspecialidades()
        );

        model.addAttribute(
                "ubicaciones",
                catalogoService.obtenerUbicaciones()
        );

        model.addAttribute(
                "horarios",
                catalogoService.obtenerHorarios()
        );

        return "index";
    }
}