package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.user.LoginResponseDTO;
import com.example.treksathi.dto.user.UploadIImageDTO;
import com.example.treksathi.dto.user.UserCreateDTO;
import com.example.treksathi.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

public interface IUserServices {
    User signup(UserCreateDTO request);

    LoginResponseDTO verify(UserCreateDTO userCreateDTO);

    LoginResponseDTO generateNewAccessToken(String refreshToken);

    // OTP
    void sendRegistrationOTP(User user);

    boolean verifyOTP(String email, String otpString);

    // PROFILE
    User uploadImageUrl(UploadIImageDTO image);

    String getProfileUrl(int id);

    // LOGOUT
    boolean logoutUser(HttpServletRequest request);

    // USER LOOKUP
    Optional<User> findByEmail(String email);

    // OAUTH2
    ResponseEntity<LoginResponseDTO> handleOAuth2LoginRequest(
            OAuth2User oAuth2User,
            String registrationId
    );
}
