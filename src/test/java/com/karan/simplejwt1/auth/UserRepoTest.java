package com.karan.simplejwt1.auth;

import com.karan.simplejwt1.auth.repo.UserRepo;
import com.karan.simplejwt1.entity.SimpleUser;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("should return user when username exists")
    public void shouldFindUserByUsername(){
        SimpleUser user = new SimpleUser();
        user.setUsername("user123");
        user.setEmail("user123@gmail.com");
        user.setPassword("password");
        user.setEnabled(true);

        testEntityManager.persistAndFlush(user);

        // when
        Optional<SimpleUser> result =
                userRepo.findByUsername("user123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("user123");
        assertThat(result.get().getEmail()).isEqualTo("user123@gmail.com");
    }

    @Test
    @DisplayName("should return empty when username not exists")
    public void shouldReturnEmptyWhenUserDoesNotExists() {
        // when
        Optional<SimpleUser> optionalSimpleUser = userRepo.findByUsername("ghost");

        //then
        assertThat(optionalSimpleUser).isEmpty();
    }

    @Test
    @DisplayName("should throw expect when username is pre-existing")
    public void shouldFailWhenUserIsDuplicate(){
        SimpleUser user1 = new SimpleUser();
        user1.setUsername("user123");
        user1.setEmail("user@gmail.com");
        user1.setPassword("password1");
        user1.setEnabled(true);


        testEntityManager.persist(user1);
        testEntityManager.flush();

        assertThrows(ConstraintViolationException.class , ()->{

            SimpleUser user2 = new SimpleUser();
            user2.setUsername("user123");
            user2.setEmail("user123@gmail.com");
            user2.setPassword("password2");
            user2.setEnabled(true);

            testEntityManager.persist(user2);
            testEntityManager.flush();
        });
//        can use this also
//        assertThatThrownBy(()->{}).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should return only enabled user")
    public void shouldFindOnlyEnabledUser(){
        SimpleUser u1 = new SimpleUser();
        u1.setUsername("user1");
        u1.setPassword("pass1");
        u1.setEnabled(true);

        SimpleUser u2 = new SimpleUser();
        u2.setUsername("user2");
        u2.setPassword("pass2");
        u2.setEnabled(false);


        testEntityManager.persist(u1);
        testEntityManager.persist(u2);
        testEntityManager.flush();

        List<SimpleUser> result = userRepo.findEnabledUsers();


        assertThat(result)
                .hasSize(1)
                .extracting(SimpleUser::getUsername)
                .containsExactly("user1");
    }
}
