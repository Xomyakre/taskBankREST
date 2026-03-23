package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CardCryptoService {

    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CardCryptoService(@Value("${app.security.crypto.secret:change-me-change-me-change-me-change-me}") String secret) {
        byte[] sha256 = sha256(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = new SecretKeySpec(sha256, "AES");
    }

    public String encrypt(String plainCardNumber) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] ciphertext = cipher.doFinal(plainCardNumber.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Card encryption failed", e);
        }
    }

    public String extractDigits(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\D", "");
    }

    public String extractLast4(String cardNumber) {
        String digits = extractDigits(cardNumber);
        if (digits.length() < 4) {
            throw new IllegalArgumentException("Card number is too short");
        }
        return digits.substring(digits.length() - 4);
    }

    public String maskFromLast4(String last4) {
        String normalized = last4 == null ? "" : last4.trim();
        if (normalized.length() != 4) {
            throw new IllegalArgumentException("Last4 must be 4 digits");
        }
        return "**** **** **** " + normalized;
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}

