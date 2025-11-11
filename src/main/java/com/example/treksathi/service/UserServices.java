package com.example.treksathi.service;

import com.example.treksathi.config.JWTService;
import com.example.treksathi.dto.user.LoginResponseDTO;
import com.example.treksathi.dto.user.UserCreateDTO;
import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.enums.Role;
import com.example.treksathi.exception.InvalidCredentialsException;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServices {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JWTService jwtService;

    @Transactional
    public User signup(UserCreateDTO request) {
        String email = request.getEmail();
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new DuplicateKeyException(String.format("User with the email address '%s' already exists", email));
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(Role.HIKER);

        return userRepository.save(user);

    }

    public String verify(UserCreateDTO userCreateDTO) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userCreateDTO.getEmail(), userCreateDTO.getPassword());

            Authentication auth = authenticationManager.authenticate((authToken));
            Optional<User> u = userRepository.findByEmail(userCreateDTO.getEmail());
            if (u.isEmpty()) {
                throw new UsernameNotFoundException("User not found with email: " + userCreateDTO.getEmail());
            }
            User user = u.get();
            if (auth.isAuthenticated()) {
                String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName(), String.valueOf(user.getRole()));
                return token;
            } else {
                throw new InvalidCredentialsException("Invalid credentials");
            }
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
//    public UserResponseDTO

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public ResponseEntity<LoginResponseDTO> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {

        AuthProvidertype providertype = jwtService.getProviderTypeFromRegistrationId(registrationId);
        String providerId = jwtService.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        User user = userRepository.findByProviderIdAndProviderType(providerId, providertype).orElse(null);
        String email = oAuth2User.getAttribute("email");
        User emailUser = userRepository.findByEmail(email).orElse(null);

        if (user == null && emailUser == null) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(oAuth2User.getAttribute("name"));
            newUser.setProviderId(providerId);
            newUser.setProviderType(providertype);
            newUser.setRole(Role.HIKER); // Default role
            user = userRepository.save(newUser);
            log.info("New OAuth2 user created: {} via {}", email, providertype);
        }
        else if (user == null && emailUser != null) {
            emailUser.setProviderId(providerId);
            emailUser.setProviderType(providertype);

            user = userRepository.save(emailUser);
            log.info("Linked OAuth2 provider {} to existing user: {}", providertype, email);
        }
        else if (user != null) {
            // User already exists with this OAuth provider
            log.info("Existing OAuth2 user logged in: {} via {}", email, providertype);

            // Update user info in case it changed (optional)
            String newName = oAuth2User.getAttribute("name");
            if (newName != null && !newName.equals(user.getName())) {
                user.setName(newName);
                user = userRepository.save(user);
            }
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                String.valueOf(user.getRole())
        );

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setId(user.getId());
        loginResponseDTO.setJwt(token);


        return ResponseEntity.ok(loginResponseDTO);

    }
}

