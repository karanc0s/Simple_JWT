package com.karan.simplejwt1.auth.repo;

import com.karan.simplejwt1.entity.SimpleRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepo extends JpaRepository<SimpleRole, Long> {

    public Optional<SimpleRole> findByRole(String role);
}
