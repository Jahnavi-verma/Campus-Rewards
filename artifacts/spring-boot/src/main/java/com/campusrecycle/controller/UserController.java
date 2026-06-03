package com.campusrecycle.controller;

import com.campusrecycle.dto.BadgeDto;
import com.campusrecycle.dto.UserDto;
import com.campusrecycle.model.User;
import com.campusrecycle.service.BadgeService;
import com.campusrecycle.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") 
public class UserController {

    private final UserService userService;
    private final BadgeService badgeService;

    // 🔗 Updated constructor injecting both services
    public UserController(UserService userService, BadgeService badgeService) {
        this.userService = userService;
        this.badgeService = badgeService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(UserDto.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserDto>> getLeaderboard() {
        List<UserDto> leaderboard = userService.findAllUsers().stream()
                .sorted((a, b) -> Integer.compare(b.getPoints(), a.getPoints()))
                .limit(20)
                .map(UserDto::from)
                .toList();
        return ResponseEntity.ok(leaderboard);
    }

    @PostMapping("/me/points")
    public ResponseEntity<UserDto> addPoints(Authentication authentication,
                                             @RequestBody Map<String, Integer> body) {
        Long userId = Long.parseLong(authentication.getName());
        int points = body.getOrDefault("points", 0);
        User user = userService.addPoints(userId, points);
        return ResponseEntity.ok(UserDto.from(user));
    }

    // 🎖️ GET /users/me/badges
    // Pulls dynamic badge statuses calculated live from submission records
    @GetMapping("/me/badges")
    public ResponseEntity<List<BadgeDto>> getMyBadges(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        int currentPoints = userService.findById(userId).map(User::getPoints).orElse(0);

        List<BadgeDto> userBadges = badgeService.calculateUserBadges(userId, currentPoints);
        return ResponseEntity.ok(userBadges);
    }
}