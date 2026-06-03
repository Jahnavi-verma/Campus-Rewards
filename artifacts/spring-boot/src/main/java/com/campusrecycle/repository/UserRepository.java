package com.campusrecycle.repository;

import com.campusrecycle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used for the login system
    Optional<User> findByEmail(String email);

    // Used to prevent duplicate accounts during registration
    Boolean existsByEmail(String email);

    // 🌟 ADD THIS: Checks if a USN is already registered in Supabase
    Boolean existsByUsn(String usn);

    // Kept for backward compatibility
    Optional<User> findByGithubId(String githubId);
}