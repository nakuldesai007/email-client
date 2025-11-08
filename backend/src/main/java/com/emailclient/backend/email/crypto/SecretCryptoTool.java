package com.emailclient.backend.email.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for encrypting SMTP secrets so they can be stored in application configuration.
 *
 * Usage:
 *   java com.emailclient.backend.email.crypto.SecretCryptoTool <masterKeyBase64> <saltBase64> <smtpPassword> [ivHex]
 *
 * If ivHex is omitted, a random 12-byte IV will be generated.
 */
public final class SecretCryptoTool {

    private static final int GCM_TAG_BITS = 128;

    private SecretCryptoTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || args.length > 4) {
            System.err.println("Usage: java " + SecretCryptoTool.class.getName()
                    + " <masterKeyBase64> <saltBase64> <smtpPassword> [ivHex]");
            System.exit(1);
        }

        byte[] masterKey = decodeBase64(args[0], "masterKey");
        if (masterKey.length != 32) {
            throw new IllegalArgumentException("Master key must be 32 bytes for AES-256");
        }

        byte[] salt = decodeBase64(args[1], "salt");
        String smtpPassword = args[2];
        byte[] iv = args.length == 4 && !args[3].isBlank()
                ? decodeHex(args[3])
                : generateRandomIv();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(masterKey, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
        cipher.updateAAD(salt);
        byte[] cipherText = cipher.doFinal(smtpPassword.getBytes(StandardCharsets.UTF_8));

        System.out.println("Encrypted password (Base64): " + Base64.getEncoder().encodeToString(cipherText));
        System.out.println("Initialization vector (Base64): " + Base64.getEncoder().encodeToString(iv));
    }

    private static byte[] decodeBase64(String payload, String label) {
        try {
            return Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(label + " must be valid Base64", ex);
        }
    }

    private static byte[] decodeHex(String hex) {
        if ((hex.length() & 1) != 0) {
            throw new IllegalArgumentException("IV hex must have an even number of characters");
        }
        int length = hex.length() / 2;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int index = i * 2;
            bytes[i] = (byte) Integer.parseInt(hex.substring(index, index + 2), 16);
        }
        return bytes;
    }

    private static byte[] generateRandomIv() throws Exception {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[12];
        random.nextBytes(iv);
        return iv;
    }
}

