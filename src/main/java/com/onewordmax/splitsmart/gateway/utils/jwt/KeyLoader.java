package com.onewordmax.splitsmart.gateway.utils.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyLoader {

    @Value("${PRIVATE_KEY}")
    private String privateKeyPem;

    @Value("${PUBLIC_KEY}")
    private String publicKeyPem;

    @Getter
    private PrivateKey privateKey;

    @Getter
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodePem(privateKeyPem, "PRIVATE KEY")));
        publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodePem(publicKeyPem, "PUBLIC KEY")));
    }

    /**
     * Accepts PEM from env: real newlines, or a single line with {@code \n} escapes.
     */
    static byte[] decodePem(String pem, String label) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException(label + " PEM is missing or empty");
        }
        String normalized = pem.trim()
                .replace("\\n", "\n")
                .replace("\\r", "");
        String body = normalized
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }
}
