package com.sky_ecommerce.auth.service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${spring.application.name:ecommerce-app}")
    private String applicationName;

    // map to .env alias APP_FRONTEND_RESETBASEURL (fallback to the old key and default)
    @Value("${app.frontend.resetBaseUrl:${APP_FRONTEND_RESETBASEURL:http://localhost:3000/reset?token=}}")
    private String resetBaseUrl;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendLoginNotification(String toEmail, @Nullable String ipAddress) {
        String subject = "[" + applicationName + "] New sign-in detected";
        String formattedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n")
            .append("A new sign-in to your account was detected.\n\n")
            .append("Time: ").append(formattedTime).append("\n");
        if (ipAddress != null && !ipAddress.isBlank()) {
            body.append("IP: ").append(ipAddress).append("\n");
        }
        body.append("\nIf this was you, no action is needed. ")
            .append("If you did not initiate this sign-in, please secure your account.\n\n")
            .append("Regards,\n")
            .append(applicationName);

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            // Set friendly sender name
            message.setFrom("skyMart <" + fromAddress + ">");
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body.toString());

        try {
            mailSender.send(message);
            log.debug("Sent login notification email to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send login notification to {}: {}", toEmail, ex.getMessage());
            throw ex;
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "[" + applicationName + "] Password reset request";
        String link = resetBaseUrl + token;

        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n")
            .append("We received a request to reset your password.\n")
            .append("You can reset your password using the link below:\n\n")
            .append(link).append("\n\n")
            .append("If you did not request a password reset, please ignore this email.\n\n")
            .append("Regards,\n")
            .append(applicationName);

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            // Set friendly sender name
            message.setFrom("skyMart <" + fromAddress + ">");
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body.toString());

        try {
            mailSender.send(message);
            log.debug("Sent password reset email to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
            throw ex;
        }
    }

    public void sendOtpEmail(String toEmail, String code) {
        String subject = "[" + applicationName + "] Verify your account";
        String body = "Hello,\n\n"
                + "Your verification code is: " + code + "\n"
                + "This code will expire in 10 minutes.\n\n"
                + "If you did not initiate this request, please ignore this email.\n\n"
                + "Regards,\n"
                + applicationName;

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            // Set friendly sender name
            message.setFrom("skyMart <" + fromAddress + ">");
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.debug("Sent OTP email to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
            throw ex;
        }
    }

    public void sendWelcomeEmail(String toEmail) {
        String subject = "[" + applicationName + "] Welcome!";
        String body = "Hello,\n\n"
                + "Your account has been successfully verified and activated.\n"
                + "Welcome to " + applicationName + "!\n\n"
                + "Regards,\n"
                + applicationName;

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            // Set friendly sender name
            message.setFrom("skyMart <" + fromAddress + ">");
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.debug("Sent welcome email to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}: {}", toEmail, ex.getMessage());
            throw ex;
        }
    }
}
