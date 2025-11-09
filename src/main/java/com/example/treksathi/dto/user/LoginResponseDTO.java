package com.example.treksathi.dto.user;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private int id;
    private String jwt;
}

