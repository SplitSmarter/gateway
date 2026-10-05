package com.onewordmax.splitsmart.gateway.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class GoogleAuthConfig {

    @Value("${GOOGLE_WEB_CLIENT_ID}")
    private String webClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        try {
            this.verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(webClientId))
                    .build();
            log.info("Google Token Verifier initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize Google Token Verifier", e);
            throw new RuntimeException(e);
        }
    }

    public GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            // verifier.verify is a BLOCKING call
            GoogleIdToken idToken = verifier.verify(idTokenString);
            return (idToken != null) ? idToken.getPayload() : null;
        } catch (Exception e) {
            log.error("Error verifying Google Token: {}", e.getMessage());
            return null;
        }
    }
}