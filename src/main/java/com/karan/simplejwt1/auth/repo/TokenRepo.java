package com.karan.simplejwt1.auth.repo;

import com.karan.simplejwt1.entity.SimpleToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepo extends JpaRepository<SimpleToken, Long> {

    Optional<SimpleToken> findByToken(String token);

}
