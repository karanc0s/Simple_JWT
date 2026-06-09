package com.karan.simplejwt1.auth.repo;

import com.karan.simplejwt1.entity.SimpleToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepo extends JpaRepository<SimpleToken, Long> {

}
