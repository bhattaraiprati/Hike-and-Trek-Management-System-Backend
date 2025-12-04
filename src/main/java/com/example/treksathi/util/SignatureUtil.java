package com.example.treksathi.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String ESEWA_SECRET_KEY = "8gBm/:&EnhH.1/q"; // Verify with eSewa UAT

    // For payment initiation
    public static String generateSignature(String totalAmount, String transactionUuid, String productCode) {
        try {
            String message = String.format(
                    "total_amount=%s,transaction_uuid=%s,product_code=%s",
                    totalAmount, transactionUuid, productCode);
            System.out.println("Signature message (initiation): " + message); // Debug log
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    ESEWA_SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);
            System.out.println("Generated signature (initiation): " + signature); // Debug log
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature: " + e.getMessage());
        }
    }

    // For success response verification
    public static String generateSignature(String transactionCode, String status, String totalAmount,
                                           String transactionUuid, String productCode, String signedFieldNames) {
        try {
            String message = String.format(
                    "transaction_code=%s,status=%s,total_amount=%s,transaction_uuid=%s,product_code=%s,signed_field_names=%s",
                    transactionCode, status, totalAmount, transactionUuid, productCode, signedFieldNames);
            System.out.println("Signature message (verification): " + message); // Debug log
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    ESEWA_SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);
            System.out.println("Generated signature (verification): " + signature); // Debug log
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature: " + e.getMessage());
        }
    }

}
