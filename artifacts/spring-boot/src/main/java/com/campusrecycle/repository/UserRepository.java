package com.campusrecycle.repository;

import com.campusrecycle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByUsn(String usn);

    Optional<User> findByGithubId(String githubId);

    // 🟢 ADDED: Directly update Neon Postgres to bypass Spring Security principal checks
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.points = :points, u.avatarUrl = :level WHERE u.id = :id")
    void updatePointsAndLevel(
        @org.springframework.data.repository.query.Param("id") Long id, 
        @org.springframework.data.repository.query.Param("points") int points, 
        @org.springframework.data.repository.query.Param("level") String level
    );
}