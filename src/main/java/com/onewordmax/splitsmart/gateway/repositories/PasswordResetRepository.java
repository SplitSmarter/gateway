package com.onewordmax.splitsmart.gateway.repositories;

import com.onewordmax.splitsmart.gateway.model.PasswordResetToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PasswordResetRepository extends ReactiveCrudRepository<PasswordResetToken, Long> {
    Mono<PasswordResetToken> findByToken(String token);
    Mono<PasswordResetToken> findByEmail(String email);
    Mono<Void> deleteByEmail(String email);
}