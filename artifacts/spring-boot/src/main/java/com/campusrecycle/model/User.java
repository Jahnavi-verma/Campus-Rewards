package com.campusrecycle.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String githubId;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Column(nullable = false)
    private String name;

    private String avatarUrl;

    // 🌟 ADD THIS FIELD: Map the USN parameter to the column configuration
    @Column(unique = true)
    private String usn;

    @Column(nullable = false)
    private int points = 0;

    @Column(nullable = false)
    private String role = "STUDENT";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLoginAt = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGithubId() { return githubId; }
    public void setGithubId(String githubId) { this.githubId = githubId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // 🌟 ADD THESE USN METHODS
    public String getUsn() { return usn; }
    public void setUsn(String usn) { this.usn = usn; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    // 🌟 ADD THIS SETTER METHOD TO SATISFY THE SERVICE COMPILER
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}