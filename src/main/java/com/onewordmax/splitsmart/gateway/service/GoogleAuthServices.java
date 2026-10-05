package com.onewordmax.splitsmart.gateway.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.onewordmax.splitsmart.gateway.config.GoogleAuthConfig;
import com.onewordmax.splitsmart.gateway.dto.external.GoogleUserPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class GoogleAuthServices {

    private final GoogleAuthConfig googleAuth;

    public Mono<GoogleUserPayload> verifyToken(String token) {
        return Mono.fromCallable(() -> {
            GoogleIdToken.Payload payload = googleAuth.verifyToken(token);
            if (payload == null) {
                return null;
            }
            return new GoogleUserPayload(
                    payload.getEmail(),
                    (String) payload.get("name"),
                    payload.getSubject() // This is the 'sub' claim
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }
}