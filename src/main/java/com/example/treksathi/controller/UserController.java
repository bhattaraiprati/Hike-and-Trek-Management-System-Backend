package com.example.treksathi.controller;

import com.example.treksathi.dto.user.UserCreateDTO;
import com.example.treksathi.dto.user.UserResponseDTO;
import com.example.treksathi.exception.InvalidCredentialsException;
import com.example.treksathi.model.User;
import com.example.treksathi.service.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserServices userServices;



    @GetMapping("/greet")
    public String greet(HttpServletRequest request){
    return "Hello from trek sathi";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateDTO user){
        try {
            User savedUser = userServices.signup(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new UserResponseDTO(savedUser));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@Valid @RequestBody UserCreateDTO userCreateDTO){
//      @Valid -> it used to check the validation of the incoming request before running into the method
        String token = userServices.verify(userCreateDTO);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }

    @GetMapping("/find")
    public ResponseEntity<?> findUser(@RequestParam String email) {

        Optional<User> user = userServices.findByEmail(email);
        if (user.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is not found");
        }
        return ResponseEntity.status(HttpStatus.FOUND).body(user);
    }

}
