package com.example.treksathi.Interfaces;

public interface TokenBlacklist {
    void addToBlackList(String token);
    boolean isBlackListed(String token);
}
