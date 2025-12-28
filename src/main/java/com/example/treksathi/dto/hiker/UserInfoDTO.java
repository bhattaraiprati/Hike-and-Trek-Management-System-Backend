package com.example.treksathi.dto.hiker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private String name;
    private String email;
    private String avatar;
    private String membershipLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    private Integer streak; // Days streak
}

