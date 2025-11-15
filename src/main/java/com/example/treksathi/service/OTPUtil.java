package com.example.treksathi.service;

import java.security.SecureRandom;

public class OTPUtil {

    private static final SecureRandom random = new SecureRandom();

    public static int generateOtp() {
        int otp = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return otp;
    }
}
