package com.example.treksathi.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkEmailResponse {
    private int totalRequested;
    private int validRecipients;
    private int invalidRecipients;
    private List<String> invalidEmails;
    private String message;
}
