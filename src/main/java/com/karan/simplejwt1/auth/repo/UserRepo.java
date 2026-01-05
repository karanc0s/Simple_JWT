package com.karan.simplejwt1.auth.repo;

import com.karan.simplejwt1.entity.SimpleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<SimpleUser, Long> {

    Optional<SimpleUser> findByUsername(String username);

    @Query("SELECT u from SimpleUser u where u.isEnabled")
    List<SimpleUser> findEnabledUsers(); // can rename to :: findAllByIsEnabledIsTrue

}

