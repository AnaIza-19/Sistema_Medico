package com.example.sistemamedico.controller;

import com.example.sistemamedico.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerificacionDpiController {

    private final UsuarioService usuarioService;

    public VerificacionDpiController(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }

    @PostMapping("/verificar-dpi")
    public ResponseEntity<Map<String, String>> verificarDpi(
            @RequestBody Map<String, String> datos) {

        String dpi = datos.get("dpi");

        String resultado =
                usuarioService.verificarDpi(dpi);

        Map<String, String> respuesta = new HashMap<>();

        switch (resultado) {

            case "DPI_INVALIDO" -> {
                respuesta.put("estado", "DPI_INVALIDO");
                respuesta.put(
                        "mensaje",
                        "El DPI debe contener exactamente 13 dígitos."
                );
            }

            case "NO_REGISTRADO" -> {
                respuesta.put("estado", "NO_REGISTRADO");
                respuesta.put(
                        "mensaje",
                        "No se encontró un registro asociado a este DPI."
                );
            }

            case "PACIENTE" -> {
                respuesta.put("estado", "PACIENTE");
                respuesta.put(
                        "mensaje",
                        "Paciente registrado correctamente."
                );
            }

            case "USUARIO_INTERNO" -> {
                respuesta.put(
                        "estado",
                        "USUARIO_INTERNO"
                );

                respuesta.put(
                        "mensaje",
                        "Este DPI pertenece a un usuario del sistema interno. Por favor, contacte a recepción."
                );
            }
        }

        return ResponseEntity.ok(respuesta);
    }
}
