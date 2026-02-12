package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IEmailSendService;
import com.example.treksathi.model.EventRegistration;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid")
public class SendGridEmailSendService implements IEmailSendService {

    private final WebClient.Builder webClientBuilder;

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendSimpleEmailAsync(String to, String subject, String text) {
        return sendEmailViaHttp(to, subject, text, null);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendBookingConfirmationEmail(EventRegistration registration) {
        String subject = "Booking Confirmed - " + registration.getEvent().getTitle();
        StringBuilder text = new StringBuilder();
        text.append("Dear ").append(registration.getContactName() != null ? registration.getContactName()
                : registration.getUser().getName()).append(",\n\n");
        text.append("Thank you for your payment!\n");
        text.append("Your registration for the event has been successfully confirmed.\n\n");
        text.append("Event Details:\n");
        text.append("• Event: ").append(registration.getEvent().getTitle()).append("\n");
        text.append("• Date: ").append(registration.getEvent().getDate()).append("\n");
        text.append("• Participants: ").append(registration.getEventParticipants().size()).append("\n");

        if (registration.getPayments() != null) {
            double amount = registration.getPayments().getAmount();
            text.append("• Amount Paid: NPR ").append(String.format("%.2f", amount)).append("\n");
        }

        text.append("• Booking ID: ").append(registration.getId()).append("\n\n");
        text.append("We're excited to see you at the event!\n");
        text.append("If you have any questions, feel free to reply to this email.\n\n");
        text.append("Best regards,\n");
        text.append(registration.getEvent().getOrganizer().getOrganization_name()).append("\n");
        text.append("Event Team\n");
        text.append("Kathmandu, Nepal");

        return sendEmailViaHttp(registration.getEmail(), subject, text.toString(), null);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendBulkEmailAsync(List<String> recipients, String subject, String text) {
        // SendGrid supports multiple recipients in one personalization, but for bulk
        // we'll loop or use BCC style personalizations
        // Simplified: Loop for each recipient to avoid complexity of complex SendGrid
        // JSON for now
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (String recipient : recipients) {
            futures.add(sendEmailViaHttp(recipient, subject, text, null));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().allMatch(f -> f.join()));
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, File attachment)
            throws MessagingException {
        try {
            sendEmailViaHttp(to, subject, text, attachment).get();
        } catch (Exception e) {
            throw new MessagingException("Failed to send email with attachment via SendGrid", e);
        }
    }

    private CompletableFuture<Boolean> sendEmailViaHttp(String to, String subject, String text, File attachment) {
        Map<String, Object> body = new HashMap<>();

        // Personalizations
        Map<String, Object> personalization = new HashMap<>();
        personalization.put("to", Collections.singletonList(Collections.singletonMap("email", to)));
        personalization.put("subject", subject);
        body.put("personalizations", Collections.singletonList(personalization));

        // From
        body.put("from", Collections.singletonMap("email", fromEmail));

        // Content
        body.put("content", Collections.singletonList(Map.of(
                "type", "text/plain",
                "value", text)));

        // Attachment
        if (attachment != null && attachment.exists()) {
            try {
                byte[] fileContent = Files.readAllBytes(attachment.toPath());
                String encodedContent = Base64.getEncoder().encodeToString(fileContent);

                Map<String, String> attachmentMap = new HashMap<>();
                attachmentMap.put("content", encodedContent);
                attachmentMap.put("filename", attachment.getName());
                attachmentMap.put("type", Files.probeContentType(attachment.toPath()));
                attachmentMap.put("disposition", "attachment");

                body.put("attachments", Collections.singletonList(attachmentMap));
            } catch (IOException e) {
                log.error("Failed to read attachment: {}", attachment.getName(), e);
            }
        }

        return webClientBuilder.build()
                .post()
                .uri(SENDGRID_API_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    log.info("Email sent via SendGrid to: {}", to);
                    return true;
                })
                .onErrorResume(e -> {
                    log.error("Failed to send email via SendGrid to: {}, Error: {}", to, e.getMessage());
                    return Mono.just(false);
                })
                .toFuture();
    }
}
