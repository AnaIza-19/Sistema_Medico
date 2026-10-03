package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Pago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private final JavaMailSender mailSender;


    @Value("${spring.mail.username}")
    private String correoRemitente;


    public CorreoService(
            JavaMailSender mailSender
    ) {

        this.mailSender = mailSender;
    }


    // =====================================================
    // CU-02
    // CORREO DE BIENVENIDA
    // =====================================================

    public void enviarCorreoBienvenida(
            String destinatario,
            String nombrePaciente
    ) {

        SimpleMailMessage mensaje =
                new SimpleMailMessage();


        mensaje.setFrom(
                correoRemitente
        );


        mensaje.setTo(
                destinatario
        );


        mensaje.setSubject(
                "Bienvenido al Sistema de Citas - Hospital Sistema Médico"
        );


        mensaje.setText(
                "Estimado(a) "
                        + nombrePaciente
                        + ", su registro ha sido completado exitosamente. "
                        + "Ya puede agendar sus citas médicas a través de nuestro portal."
        );


        mailSender.send(
                mensaje
        );
    }


    // =====================================================
    // CU-04
    // COMPROBANTE DE PAGO
    // =====================================================

    public void enviarComprobantePago(
            Pago pago
    ) {

        String correoPaciente =
                pago.getCita()
                        .getPaciente()
                        .getCorreo();


        String nombrePaciente =
                pago.getCita()
                        .getPaciente()
                        .getNombre()
                        + " "
                        + pago.getCita()
                        .getPaciente()
                        .getApellido();


        String medico =
                pago.getCita()
                        .getMedico()
                        .getNombre()
                        + " "
                        + pago.getCita()
                        .getMedico()
                        .getApellido();


        String especialidad =
                pago.getCita()
                        .getEspecialidad()
                        .getNombre();


        String sucursal =
                pago.getCita()
                        .getSucursal()
                        .getNombre();


        String fecha =
                pago.getCita()
                        .getHorario()
                        .getFecha()
                        .toString();


        String hora =
                pago.getCita()
                        .getHorario()
                        .getHoraInicio()
                        + " - "
                        + pago.getCita()
                        .getHorario()
                        .getHoraFin();


        SimpleMailMessage mensaje =
                new SimpleMailMessage();


        mensaje.setFrom(
                correoRemitente
        );


        mensaje.setTo(
                correoPaciente
        );


        mensaje.setSubject(
                "Comprobante de pago - Sistema Médico Hospitalario"
        );


        mensaje.setText(
                "Estimado(a) " + nombrePaciente + ",\n\n"
                        + "Su pago ha sido realizado exitosamente.\n\n"
                        + "COMPROBANTE DE PAGO\n"
                        + "----------------------------------\n"
                        + "Número de transacción: "
                        + pago.getNumeroTransaccion() + "\n"
                        + "Médico: "
                        + medico + "\n"
                        + "Especialidad: "
                        + especialidad + "\n"
                        + "Sucursal: "
                        + sucursal + "\n"
                        + "Fecha: "
                        + fecha + "\n"
                        + "Hora: "
                        + hora + "\n"
                        + "Monto pagado: Q"
                        + pago.getMonto() + "\n"
                        + "Tarjeta terminada en: **** "
                        + pago.getUltimosCuatro() + "\n"
                        + "----------------------------------\n\n"
                        + "Su cita ha sido confirmada.\n\n"
                        + "Gracias por utilizar el Sistema Médico Hospitalario."
        );


        mailSender.send(
                mensaje
        );
    }
}