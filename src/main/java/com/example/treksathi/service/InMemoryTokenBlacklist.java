package com.example.treksathi.service;


import com.example.treksathi.Interfaces.TokenBlacklist;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class InMemoryTokenBlacklist implements TokenBlacklist {
    private Set<String> blackList = new HashSet<>();
    @Override
    public void addToBlackList(String token){
        blackList.add(token);
    }

    @Override
    public boolean isBlackListed(String token){
        return blackList.contains(token);
    }
}
