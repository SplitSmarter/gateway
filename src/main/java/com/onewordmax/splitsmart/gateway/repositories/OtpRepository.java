package com.onewordmax.splitsmart.gateway.repositories;

import com.onewordmax.splitsmart.gateway.model.SignupOtp;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OtpRepository extends ReactiveCrudRepository<SignupOtp, Long> {

    Mono<SignupOtp> findByEmail(String email);
}
