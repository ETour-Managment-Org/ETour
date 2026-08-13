package com.etour.email;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.etour.entities.Booking;
import com.etour.entities.User;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailProperties properties;

    @Value("${etour.site-url:http://localhost:5173}")
    private String siteUrl;

    public EmailServiceImpl(JavaMailSender mailSender, EmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        if (user == null || user.getEmail() == null) {
            return;
        }
        send(user.getEmail(),
             EmailTemplates.welcomeSubject(),
             EmailTemplates.welcome(user, properties),
             "welcome");
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(User user, boolean changedByAdmin) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        send(user.getEmail(),
             EmailTemplates.passwordChangedSubject(),
             EmailTemplates.passwordChanged(user, properties, changedByAdmin, siteUrl),
             "password change notice for " + user.getUsername());
    }

    @Override
    @Async
    public void sendBookingInvoice(Booking booking) {
        String to = recipientFor(booking);
        if (to == null) {
            log.warn("Booking {} has no e-mail address, invoice not sent",
                    booking == null ? "?" : booking.getBookingId());
            return;
        }
        send(to,
             EmailTemplates.invoiceSubject(booking),
             EmailTemplates.invoice(booking, properties),
             "invoice for booking " + booking.getBookingId());
    }

    private static String recipientFor(Booking booking) {
        if (booking == null) {
            return null;
        }
        String email = booking.resolveNotificationEmail();
        return email == null || email.isBlank() ? null : email;
    }

    private void send(String to, String subject, String html, String what) {
        String recipient = properties.hasOverride() ? properties.getOverrideRecipient() : to;

        if (!properties.isEnabled()) {
            log.info("[MAIL DISABLED] would send '{}' to {} ({})", subject, recipient, what);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(properties.getFrom(), properties.getFromName());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Sent '{}' to {}", subject, recipient);

        } catch (Exception ex) {
            log.error("Could not send {} to {}: {}", what, recipient, ex.getMessage());
        }
    }

    @Override
    @Async
    public void sendCancellationEmail(Booking booking, BigDecimal refundAmount, String policyNote) {
        String to = recipientFor(booking);
        if (to == null) {
            return;
        }
        send(to,
             EmailTemplates.cancellationSubject(booking),
             EmailTemplates.cancellation(booking, properties, refundAmount, policyNote),
             "cancellation notice for booking " + booking.getBookingId());
    }

    @Override
    @Async
    public void sendTourCompletedEmail(Booking booking) {
        String to = recipientFor(booking);
        if (to == null) {
            return;
        }
        send(to,
             EmailTemplates.completedSubject(booking),
             EmailTemplates.tourCompleted(booking, properties, siteUrl),
             "tour completed notice for booking " + booking.getBookingId());
    }
}
