package com.api.Support.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendTicketCreatedEmail(String to, String subject, UUID ticketId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Ticket de soporte recibido - #" + ticketId);
            message.setText(
                "Hola,\n\n" +
                "Tu solicitud de soporte ha sido recibida correctamente.\n\n" +
                "Asunto: " + subject + "\n" +
                "ID del ticket: " + ticketId + "\n\n" +
                "Nuestro equipo revisará tu caso y te responderá a la brevedad.\n\n" +
                "Vexio Multi Store - Soporte"
            );
            mailSender.send(message);
            log.info("Email de creación de ticket enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de creación de ticket a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendTicketRepliedEmail(String to, String ticketSubject, UUID ticketId, String reply) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Respuesta a tu ticket de soporte - #" + ticketId);
            message.setText(
                "Hola,\n\n" +
                "Tu ticket de soporte ha recibido una nueva respuesta.\n\n" +
                "Asunto: " + ticketSubject + "\n" +
                "ID del ticket: " + ticketId + "\n\n" +
                "Respuesta del equipo de soporte:\n" +
                reply + "\n\n" +
                "Vexio Multi Store - Soporte"
            );
            mailSender.send(message);
            log.info("Email de respuesta enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de respuesta a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendTicketClosedEmail(String to, String subject, UUID ticketId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Tu ticket de soporte fue cerrado - #" + ticketId);
            message.setText(
                "Hola,\n\n" +
                "Tu ticket de soporte ha sido cerrado.\n\n" +
                "Asunto: " + subject + "\n" +
                "ID del ticket: " + ticketId + "\n\n" +
                "Si el problema persiste o tienes nuevas dudas, no dudes en abrir un nuevo ticket.\n\n" +
                "Vexio Multi Store - Soporte"
            );
            mailSender.send(message);
            log.info("Email de cierre de ticket enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email de cierre de ticket a {}: {}", to, e.getMessage());
        }
    }
}
