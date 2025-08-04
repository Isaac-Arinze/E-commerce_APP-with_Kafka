package com.sky_ecommerce.monitor;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;

import java.util.Properties;

/**
 * HealthIndicator for SMTP using the configured JavaMailSender.
 * This performs a lightweight SMTP connectivity/auth check by opening and closing a Transport.
 */
@Component
public class MailHealthIndicator implements HealthIndicator {

    private final JavaMailSender mailSender;

    public MailHealthIndicator(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Health health() {
        try {
            checkSmtpConnectivity(null);
            return Health.up().withDetail("smtp", "reachable").build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("smtp", "unreachable")
                    .withDetail("error", ex.getClass().getSimpleName() + ": " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Creates a Transport from the JavaMailSender's Session and attempts to connect.
     * If the sender is configured with username/password, this will attempt authenticated login.
     */
    private void checkSmtpConnectivity(@Nullable Integer timeoutMs) throws MessagingException {
        // Extract the underlying JavaMail Session
        Session session = (Session) org.springframework.util.ReflectionUtils
                .invokeMethod(org.springframework.util.ReflectionUtils.findMethod(mailSender.getClass(), "getSession"), mailSender);

        if (session == null) {
            // Fallback: create a simple Session from properties (shouldn't usually happen with JavaMailSenderImpl)
            Properties props = new Properties();
            session = Session.getInstance(props);
        }

        if (timeoutMs != null) {
            session.getProperties().put("mail.smtp.connectiontimeout", timeoutMs);
            session.getProperties().put("mail.smtp.timeout", timeoutMs);
            session.getProperties().put("mail.smtp.writetimeout", timeoutMs);
        }

        String protocol = session.getProperty("mail.transport.protocol");
        if (protocol == null || protocol.isBlank()) {
            protocol = "smtp";
        }

        String host = session.getProperty("mail." + protocol + ".host");
        String portStr = session.getProperty("mail." + protocol + ".port");
        String user = session.getProperty("mail." + protocol + ".user");
        if (user == null) {
            user = session.getProperty("mail.user");
        }
        String password = session.getProperty("mail.password");

        Transport transport = null;
        try {
            transport = session.getTransport(protocol);
            if (host != null && portStr != null) {
                int port = Integer.parseInt(portStr);
                if (user != null && password != null && !user.isBlank() && !password.isBlank()) {
                    transport.connect(host, port, user, password);
                } else if (user != null && !user.isBlank()) {
                    // Some servers allow user without explicit password if configured via JNDI or OAuth; try it.
                    transport.connect(host, port, user, null);
                } else {
                    // Anonymous connect (will succeed if server allows EHLO without auth)
                    transport.connect(host, port, null, null);
                }
            } else {
                // Let JavaMail resolve defaults from the session
                if (user != null && password != null) {
                    transport.connect(user, password);
                } else {
                    transport.connect();
                }
            }
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
