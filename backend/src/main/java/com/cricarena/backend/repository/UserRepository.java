package com.cricarena.backend.repository;

import com.cricarena.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByIdAndActiveTrue(Long id);

    boolean existsByEmail(String email);
}
