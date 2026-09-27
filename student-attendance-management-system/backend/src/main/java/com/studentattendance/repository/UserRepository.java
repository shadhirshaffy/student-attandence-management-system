package com.studentattendance.repository;

import java.util.Optional;

import com.studentattendance.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = {"student", "lecturer", "admin"})
    Optional<User> findWithProfilesById(Long id);
}
