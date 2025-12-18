package com.example.treksathi.service;

import com.example.treksathi.Interfaces.IRefreshTokenService;
import com.example.treksathi.exception.InternalServerErrorException;
import com.example.treksathi.exception.UsernameNotFoundException;
import com.example.treksathi.model.RefreshToken;
import com.example.treksathi.model.User;
import com.example.treksathi.record.BookingResponseRecord;
import com.example.treksathi.repository.RefreshTokenRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;


    public RefreshToken createRefreshToken(String email){
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findByEmail(email).orElse(null))
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(2))
                .build();
        return  refreshTokenRepository.save(refreshToken);
    }

    public void findTokenByUser(User user) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUser(user);
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
            // Force immediate flush and commit of the delete
            refreshTokenRepository.flush();  // Ensures DELETE SQL is sent now
        }
    }

    public Optional<RefreshToken> findByToken(String token){
        return  refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(token);
            throw  new InternalServerErrorException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

    public void deleteAllRefreshTokensForUser(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
    }

    public boolean deleteRefreshToken(User user){
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElseThrow(()-> new UsernameNotFoundException("User Not Found"));
        refreshTokenRepository.delete(refreshToken);
        return true;
    }


}
