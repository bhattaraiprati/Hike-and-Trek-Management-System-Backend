package com.example.treksathi.Interfaces;

import com.example.treksathi.model.RefreshToken;
import com.example.treksathi.model.User;

import java.util.Optional;

public interface IRefreshTokenService {
    RefreshToken createRefreshToken(String email);

    void findTokenByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteAllRefreshTokensForUser(User user);

    boolean deleteRefreshToken(User user);
}
