package com.carrefourconnect.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi d'emails pour les notifications importantes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email:admin@carrefourconnect.com}")
    private String adminEmail;

    @Value("${spring.mail.username:no-reply@carrefourconnect.com}")
    private String fromEmail;

    /**
     * Envoie une notification par email à l'admin lors d'une nouvelle inscription de commerce.
     */
    @Async
    public void envoyerNotificationNouveauCommerce(String nomCommerce, String nomCommercant, String emailCommercant) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("[CarrefourConnect] Nouvelle demande d'inscription de commerce");
            message.setText(
                "Bonjour,\n\n" +
                "Une nouvelle demande d'inscription de commerce a été soumise sur la plateforme CarrefourConnect.\n\n" +
                "Détails de la demande :\n" +
                "  • Nom du commerce : " + nomCommerce + "\n" +
                "  • Commerçant : " + nomCommercant + "\n" +
                "  • Email : " + emailCommercant + "\n\n" +
                "Connectez-vous à votre tableau de bord Admin pour valider ou rejeter cette demande :\n" +
                "http://localhost:4200/admin/validation\n\n" +
                "Cordialement,\n" +
                "L'équipe CarrefourConnect"
            );
            mailSender.send(message);
            log.info("Email de notification envoyé à l'admin pour le commerce: {}", nomCommerce);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
            // Ne pas propager l'erreur pour ne pas bloquer la création du commerce
        }
    }

    /**
     * Envoie un email au commerçant pour l'informer du rejet de sa demande.
     */
    @Async
    public void envoyerEmailRejetCommerce(String emailCommercant, String nomCommerce, String motif) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(emailCommercant);
            message.setSubject("[CarrefourConnect] Votre demande d'inscription a été rejetée");
            message.setText(
                "Bonjour,\n\n" +
                "Nous vous informons que votre demande d'inscription pour le commerce \"" + nomCommerce + "\" " +
                "a été rejetée par notre équipe d'administration.\n\n" +
                "Motif du rejet :\n" + motif + "\n\n" +
                "Si vous pensez qu'il s'agit d'une erreur ou souhaitez soumettre une nouvelle demande " +
                "avec des informations corrigées, n'hésitez pas à nous contacter.\n\n" +
                "Cordialement,\n" +
                "L'équipe CarrefourConnect"
            );
            mailSender.send(message);
            log.info("Email de rejet envoyé au commerçant: {}", emailCommercant);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de rejet: {}", e.getMessage());
        }
    }

    /**
     * Envoie un email au commerçant pour l'informer de la suspension de son commerce.
     */
    @Async
    public void envoyerEmailSuspensionCommerce(String emailCommercant, String nomCommerce, String motif) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(emailCommercant);
            message.setSubject("[CarrefourConnect] Votre commerce a été suspendu");
            message.setText(
                "Bonjour,\n\n" +
                "Nous vous informons que votre commerce \"" + nomCommerce + "\" " +
                "a été temporairement suspendu de la plateforme CarrefourConnect.\n\n" +
                "Motif de la suspension :\n" + motif + "\n\n" +
                "Pour toute question ou contestation, veuillez contacter notre équipe support.\n\n" +
                "Cordialement,\n" +
                "L'équipe CarrefourConnect"
            );
            mailSender.send(message);
            log.info("Email de suspension envoyé au commerçant: {}", emailCommercant);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de suspension: {}", e.getMessage());
        }
    }
}
