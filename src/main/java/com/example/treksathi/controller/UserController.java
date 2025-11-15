package com.example.treksathi.controller;

import com.example.treksathi.dto.user.OTPVerificationDTO;
import com.example.treksathi.dto.user.UserCreateDTO;
import com.example.treksathi.dto.user.UserResponseDTO;
import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.exception.InvalidCredentialsException;
import com.example.treksathi.exception.UsernameNotFoundException;
import com.example.treksathi.model.User;
import com.example.treksathi.service.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserServices userServices;

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
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody OTPVerificationDTO otpData) {
        try {
            boolean verified = userServices.verifyOTP(otpData.getEmail(), otpData.getOtp());

            if (verified) {
                return ResponseEntity.ok(Map.of(
                        "message", "Email verified successfully. You can now login.",
                        "verified", true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid or expired OTP"));
            }

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("OTP verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verification failed. Please try again."));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOTP(@RequestParam String email) {
        try {
            User user = userServices.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if user is already verified
            if (user.getStatus() == AccountStatus.ACTIVE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email is already verified"));
            }

            userServices.sendRegistrationOTP(user);

            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent successfully to your email",
                    "email", email
            ));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to resend OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send OTP. Please try again."));
        }
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
