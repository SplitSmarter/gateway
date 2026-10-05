package com.onewordmax.splitsmart.gateway.repositories;

import com.onewordmax.splitsmart.gateway.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByEmail(String email);

    Mono<User> findByUsername(String username);

    Mono<User> findByIdAndStatus(Long id, User.Status status);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByUsername(String username);

    Mono<Long> countByUsernameStartingWith(String prefix);
}
