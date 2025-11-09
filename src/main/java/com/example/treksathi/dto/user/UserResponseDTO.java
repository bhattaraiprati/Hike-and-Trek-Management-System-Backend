package com.example.treksathi.dto.user;

import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.model.User;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private int id;
    private String email;
    private String name;
    private String role;
    private String token;
    private AuthProvidertype providerType;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
    }
}
