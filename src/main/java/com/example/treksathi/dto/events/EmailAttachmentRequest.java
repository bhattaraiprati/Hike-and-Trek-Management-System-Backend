package com.example.treksathi.dto.events;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class EmailAttachmentRequest {

    private List<String> recipients;
    private String subject;
    private String text;
}
