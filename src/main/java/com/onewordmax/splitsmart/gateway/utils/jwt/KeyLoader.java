package com.onewordmax.splitsmart.gateway.utils.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyLoader {

    @Value("${PRIVATE_KEY_PATH}")
    private String PRIVATE_KEY_PATH;

    @Value("${PUBLIC_KEY_PATH}")
    private String PUBLIC_KEY_PATH;

    @Getter
    private PrivateKey privateKey;

    @Getter
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_PATH));
        String privateKeyPem = new String(privateKeyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodedPrivate = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedPrivate);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(privateKeySpec);

        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_PATH));
        String publicKeyPem = new String(publicKeyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decodedPublic = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedPublic);
        publicKey = keyFactory.generatePublic(publicKeySpec);
    }
}
