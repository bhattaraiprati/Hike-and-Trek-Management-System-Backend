package com.example.treksathi.Interfaces;

import com.example.treksathi.model.EventRegistration;
import jakarta.mail.MessagingException;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IEmailSendService {


    CompletableFuture<Boolean> sendSimpleEmailAsync(String to, String subject, String text);
    CompletableFuture<Boolean> sendBookingConfirmationEmail(EventRegistration eventRegistration);
    CompletableFuture<Boolean> sendBulkEmailAsync(List<String> recipients, String subject, String text);
    void sendEmailWithAttachment(String to, String subject, String text, File attachments) throws MessagingException;
}
