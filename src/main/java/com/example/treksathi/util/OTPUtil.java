package com.example.treksathi.util;

import com.example.treksathi.Interfaces.IOTPUtil;

import java.security.SecureRandom;

public class OTPUtil implements IOTPUtil {

    private static final SecureRandom random = new SecureRandom();

    @Override
    public int generateOtp() {
        int otp = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return otp;
    }
}
