package com.emailclient.backend.email.crypto;

import com.emailclient.backend.email.EmailClientProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Component
public class SecretCrypto {

    private static final Logger log = LoggerFactory.getLogger(SecretCrypto.class);
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;

    private final EmailClientProperties properties;

    private SecretKey secretKey;
    private byte[] associatedData;

    public SecretCrypto(EmailClientProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @PostConstruct
    void initialize() {
        byte[] masterKeyBytes = decodeBase64("crypto.master-key", properties.getCrypto().getMasterKey());
        if (masterKeyBytes.length != 32) {
            throw new IllegalStateException("AES-256 requires a 32-byte master key");
        }
        this.secretKey = new SecretKeySpec(masterKeyBytes, "AES");
        this.associatedData = decodeBase64("crypto.salt", properties.getCrypto().getSalt());
    }

    public String decrypt(String encryptedPayloadBase64, String initializationVectorBase64) {
        byte[] cipherText = decodeBase64("smtp.encrypted-password", encryptedPayloadBase64);
        byte[] iv = decodeBase64("smtp.initialization-vector", initializationVectorBase64);
        if (iv.length != 12 && iv.length != 16) {
            throw new IllegalArgumentException("Initialization vector must be 96 or 128 bits");
        }

        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            cipher.updateAAD(associatedData);
            byte[] plaintext = cipher.doFinal(cipherText);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Failed to decrypt secret", ex);
            throw new IllegalStateException("Unable to decrypt encrypted secret", ex);
        }
    }

    private byte[] decodeBase64(String property, String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Configuration property '%s' must not be blank".formatted(property));
        }
        try {
            return Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Configuration property '%s' must be valid Base64".formatted(property), ex);
        }
    }
}

