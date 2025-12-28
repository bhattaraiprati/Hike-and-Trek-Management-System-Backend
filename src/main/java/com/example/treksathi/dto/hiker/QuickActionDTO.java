package com.example.treksathi.dto.hiker;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionDTO {
    private Integer id;
    private String title;
    private String description;
    private String icon;
    private String path;
    private String color;
    private Integer count; // For badges (unread messages, etc.)
}
