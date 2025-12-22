package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IEmailSendService;
import com.example.treksathi.Interfaces.IOTPUtil;
import com.example.treksathi.Interfaces.IRefreshTokenService;
import com.example.treksathi.Interfaces.IUserServices;
import com.example.treksathi.config.JWTService;
import com.example.treksathi.dto.user.LoginResponseDTO;
import com.example.treksathi.dto.user.UploadIImageDTO;
import com.example.treksathi.dto.user.UserCreateDTO;
import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.enums.Role;
import com.example.treksathi.exception.InvalidCredentialsException;
import com.example.treksathi.exception.OTPNotFoundException;
import com.example.treksathi.exception.UnauthorizedException;
import com.example.treksathi.exception.UserAlreadyExistException;
import com.example.treksathi.model.OTP;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.RefreshToken;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.OTPRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServices implements IUserServices {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final OTPRepository otpRepository;
    private final IEmailSendService emailSendService;
    private final OrganizerRepository organizerRepository;
    private final JWTService jwtService;
    private final IOTPUtil otpUtil;
    private final IRefreshTokenService refreshTokenService;
    private final InMemoryTokenBlacklist inMemoryTokenBlacklist;


    @Transactional
    public User signup(UserCreateDTO request) {
        try{
            String email = request.getEmail();
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                throw new UserAlreadyExistException(String.format("User with the email address '%s' already exists", email));
            }
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            User user = new User();
            user.setName(request.getName());
            user.setEmail(email);
            user.setPassword(hashedPassword);
            user.setRole(Role.HIKER); // default if invalid
            user = userRepository.save(user);
            try{
                sendRegistrationOTP(user);
            }catch (Exception e){
                log.error("Failed to initiate OTP sending for user: {}", user.getId(), e);
            }

            return user;
        }
        catch (Exception e){
            throw new InvalidCredentialsException("Unable to register the user");
        }
    }

//    public

    @Transactional
    public LoginResponseDTO verify(UserCreateDTO userCreateDTO) {
        try {

            Optional<User> userOpt = userRepository.findByEmail(userCreateDTO.getEmail());

            if (userOpt.isEmpty()) {
                throw new UsernameNotFoundException("User not found with email: " + userCreateDTO.getEmail());
            }
            User user = userOpt.get();
            // if user is an organizer and verify approval status BEFORE authentication
            if (user.getRole() == Role.ORGANIZER) {
                Organizer organizer = organizerRepository.findByUser(user);

                if (organizer != null && organizer.getApproval_status() == Approval_status.PENDING) {
                    throw new UnauthorizedException("Your account is under review. Please wait for admin approval.");
                }

                if (organizer != null && organizer.getApproval_status() == Approval_status.DECLINE) {
                    throw new UnauthorizedException("Your account has been rejected. Please contact support.");
                }
            }
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userCreateDTO.getEmail(), userCreateDTO.getPassword());

            Authentication auth = authenticationManager.authenticate(authToken);

            if (auth.isAuthenticated()) {
                // Delete any existing refresh token before creating a new one
                refreshTokenService.findTokenByUser(user);

                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

                String token = jwtService.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        String.valueOf(user.getRole())
                );

                return LoginResponseDTO.builder()
                        .token(token)
                        .refreshToken(refreshToken.getToken())
                        .build();
            } else {
                throw new InvalidCredentialsException("Invalid credentials");
            }

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public LoginResponseDTO generateNewAccessToken( String token){
        RefreshToken refreshToken = refreshTokenService.findByToken(token).orElseThrow( () -> new RuntimeException( "Refresh Token is not in DB."));
        return refreshTokenService.findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    User user1 = userRepository.findByEmail(user.getEmail()).orElse(null);
                    String jwt = jwtService.generateToken(user1.getId(), user1.getEmail(), user1.getName(), String.valueOf(user1.getRole()));
                    return LoginResponseDTO.builder()
                            .token(jwt)
                            .refreshToken(token)
                            .build();
                }).orElseThrow(() ->new RuntimeException("Refresh Token is not in DB..!!"));

    }


    @Transactional
    public void sendRegistrationOTP(User user) {
        // Delete existing OTP if any exist
        otpRepository.findByUser(user).ifPresent(otpRepository::delete);

        // Generate and save new OTP
        int otpValue = otpUtil.generateOtp();
        OTP otp = new OTP();
        otp.setUser(user);
        otp.setOtp(otpValue);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpireAt(LocalDateTime.now().plusMinutes(5));
        otp.setUsed(false);
        otpRepository.save(otp);


        String subject = "Account Verification OTP";
        String text = String.format(
                "Hello %s,\n\n" +
                        "Your OTP for email verification is: %d\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "If you didn't request this, please ignore this email.",
                user.getName(), otpValue
        );

        emailSendService.sendSimpleEmailAsync(user.getEmail(), subject, text)
                .exceptionally(ex -> {
                    log.error("Failed to send OTP email to user: {}", user.getId(), ex);
                    return false;
                });
    }

    @Transactional
    public boolean verifyOTP(String email, String otpString) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Parse OTP
        int otpValue;
        try {
            otpValue = Integer.parseInt(otpString);
        } catch (NumberFormatException e) {
            log.error("Invalid OTP format: {}", otpString);
            return false;
        }

        // Find OTP record
        OTP otpRecord = otpRepository.findByUser(user)
                .orElseThrow(() -> new OTPNotFoundException("OTP not found for this user"));

        // Check if OTP is already used
        if (otpRecord.isUsed()) {
            log.warn("OTP already used for user: {}", user.getId());
            return false;
        }

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(otpRecord.getExpireAt())) {
            log.warn("OTP expired for user: {}", user.getId());
            return false;
        }

        // Verify OTP
        if (otpRecord.getOtp() != otpValue) {
            log.warn("Invalid OTP for user: {}", user.getId());
            return false;
        }

        // Mark OTP as used
        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);

        // Update user status to ACTIVE
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        log.info("OTP verified successfully for user: {}", user.getId());
        return true;
    }

    public User uploadImageUrl(UploadIImageDTO image){
        User user = userRepository.findById(image.getId()).orElse(null);
        user.setProfileImage(image.getImage());
        return userRepository.save(user);
    }
    public String getProfileUrl(int id){
        User user = userRepository.findById(id).orElse(null);

        return user.getProfileImage();
    }

    public boolean logoutUser(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        log.info("The authentication header"+ authorizationHeader);
        try{
            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")){
                String token = authorizationHeader.substring(7);
                log.info("The authentication token"+ token);
                inMemoryTokenBlacklist.addToBlackList(token);
                String email = jwtService.getUsernameFormToken(token);
                User user = userRepository.findByEmail(email).orElse(null);
                refreshTokenService.deleteRefreshToken(user);
                SecurityContextHolder.clearContext();
                return true;
            }
        }
        catch (Exception e){
            throw new RuntimeException("Exception Occur"+e);
        }
        return false;
    }

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

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        // Generate JWT token
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                String.valueOf(user.getRole())
        );
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken(token);
        loginResponseDTO.setRefreshToken(refreshToken.getToken());
        return ResponseEntity.ok(loginResponseDTO);

    }
}

