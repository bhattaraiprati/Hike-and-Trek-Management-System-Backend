package com.example.treksathi.service;

import com.example.treksathi.dto.user.JWTService;
import com.example.treksathi.dto.user.UserCreateDTO;
import com.example.treksathi.exception.InvalidCredentialsException;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServices {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JWTService jwtService;

    @Transactional
    public User signup(UserCreateDTO request){
        String email = request.getEmail();
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()){
            throw new DuplicateKeyException(String.format("User with the email address '%s' already exists",email));
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole("USER");

        return userRepository.save(user);

    }

    public String verify (UserCreateDTO userCreateDTO){
        try{
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userCreateDTO.getEmail(), userCreateDTO.getPassword());

            Authentication auth = authenticationManager.authenticate((authToken));
            Optional<User> u = userRepository.findByEmail(userCreateDTO.getEmail());
            if (u.isEmpty()) {
                throw new UsernameNotFoundException("User not found with email: " + userCreateDTO.getEmail());
            }
            User user = u.get();
            if(auth.isAuthenticated()){
                String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getName(), user.getRole());
                return token;
            }
            else {
                throw new InvalidCredentialsException("Invalid credentials");
            }
        }
        catch (BadCredentialsException e){
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }
//    public UserResponseDTO

    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }


}
