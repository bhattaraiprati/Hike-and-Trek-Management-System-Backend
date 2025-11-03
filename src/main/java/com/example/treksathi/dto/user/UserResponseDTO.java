package com.example.treksathi.dto.user;

import com.example.treksathi.model.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserResponseDTO {
    private int id;
    private String email;
    private String name;
    private String role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
    }
}
