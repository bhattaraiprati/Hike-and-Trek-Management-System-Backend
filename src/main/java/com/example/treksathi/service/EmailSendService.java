package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IEmailSendService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService implements IEmailSendService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String email;

    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendSimpleEmailAsync(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(email);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send email to: {}, Error: {}", to, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendBulkEmailAsync(List<String> recipients, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setBcc(recipients.toArray(new String[0])); // Use BCC to hide recipients from each other
            message.setSubject(subject);
            message.setText(text);
            //get from properties
            message.setFrom(email);

            javaMailSender.send(message);
            log.info("Bulk email sent successfully to {} recipients", recipients.size());
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send bulk email. Error: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String text, File attachment) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        //
        helper.setFrom(email);

        if (attachment != null && attachment.exists()) {
            helper.addAttachment(attachment.getName(), attachment);
        }

        javaMailSender.send(message);
    }
}

