package com.example.treksathi.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class UploadIImageDTO {
    private int id;
    private String image;
}
