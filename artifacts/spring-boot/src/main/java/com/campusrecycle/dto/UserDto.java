package com.campusrecycle.dto;

import com.campusrecycle.model.User;
import com.campusrecycle.util.LevelUtils;
import java.time.LocalDateTime;

public class UserDto {

    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private int points;
    private String role;
    private int level;
    private String levelTitle;
    private int nextLevelPoints;
    private int levelProgressPercent;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.name = user.getName();
        dto.points = user.getPoints();
        dto.role = user.getRole();
        dto.createdAt = user.getCreatedAt();
        dto.lastLoginAt = user.getLastLoginAt();

        // 🌳 COMPUTE LEVEL META DETAILS VIA LEVELUTILS
        LevelUtils.LevelInfo info = LevelUtils.getLevel(user.getPoints());
        dto.level = info.level();
        dto.levelTitle = info.title();
        dto.nextLevelPoints = info.nextLevelPoints();
        dto.levelProgressPercent = info.progressPercent();

        // ✨ ALIGN AVATAR URL WITH THE LIVE COMPUTED LEVEL TITLE STRING
        dto.avatarUrl = (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) 
                        ? user.getAvatarUrl() 
                        : info.title();

        return dto;
    }

    // 💡 Public Getters for JSON Serialization Pipeline
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getPoints() { return points; }
    public String getRole() { return role; }
    public int getLevel() { return level; }
    public String getLevelTitle() { return levelTitle; }
    public int getNextLevelPoints() { return nextLevelPoints; }
    public int getLevelProgressPercent() { return levelProgressPercent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}