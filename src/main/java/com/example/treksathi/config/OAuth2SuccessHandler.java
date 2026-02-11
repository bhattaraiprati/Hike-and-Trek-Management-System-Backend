package com.example.treksathi.config;

import com.example.treksathi.Interfaces.IUserServices;
import com.example.treksathi.dto.user.LoginResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Lazy
    private final IUserServices userServices;

    // Frontend URL - you can make this configurable via application.properties
    private static final String FRONTEND_URL = "https://hikesathi.netlify.app/";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("OAuth2 authentication successful");

        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = token.getPrincipal();
            String registrationId = token.getAuthorizedClientRegistrationId();

            log.info("Processing OAuth2 login for provider: {}", registrationId);

            // Get login response from your service
            ResponseEntity<LoginResponseDTO> loginResponse =
                    userServices.handleOAuth2LoginRequest(oAuth2User, registrationId);

            LoginResponseDTO loginData = loginResponse.getBody();

            if (loginData != null && loginData.getToken() != null) {
                // Build redirect URL with tokens as query parameters
                String redirectUrl = UriComponentsBuilder
                        .fromUriString(FRONTEND_URL + "/auth/callback")
                        .queryParam("token", loginData.getToken())
                        .queryParam("refreshToken", loginData.getRefreshToken())
                        .build()
                        .toUriString();

                log.info("Redirecting to frontend: {}", FRONTEND_URL + "/auth/callback");
                response.sendRedirect(redirectUrl);
            } else {
                // Handle error case
                log.error("Login response is null or missing token");
                redirectToErrorPage(response, "Authentication failed");
            }

        } catch (Exception e) {
            log.error("Error during OAuth2 success handling", e);
            redirectToErrorPage(response, "Authentication error: " + e.getMessage());
        }
    }

    private void redirectToErrorPage(HttpServletResponse response, String errorMessage)
            throws IOException {
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String redirectUrl = FRONTEND_URL + "/login?error=" + encodedError;
        response.sendRedirect(redirectUrl);
    }
}