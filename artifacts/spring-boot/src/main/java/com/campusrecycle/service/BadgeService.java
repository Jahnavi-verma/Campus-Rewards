package com.campusrecycle.service;

import com.campusrecycle.dto.BadgeDto;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.repository.RecyclingSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BadgeService {

    private final RecyclingSubmissionRepository submissionRepository;

    public BadgeService(RecyclingSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public List<BadgeDto> calculateUserBadges(Long userId, int currentPoints) {
        // 🔄 Fixed to use s.getUser().getId() to match your object relationship model!
        List<RecyclingSubmission> submissions = submissionRepository.findAll().stream()
                .filter(s -> s.getUser() != null && s.getUser().getId().equals(userId))
                .toList();

        int totalPlastics = submissions.stream()
                .filter(s -> s.getItemType().equalsIgnoreCase("Plastic Bottle"))
                .mapToInt(RecyclingSubmission::getQuantity)
                .sum();

        int totalMetals = submissions.stream()
                .filter(s -> s.getItemType().equalsIgnoreCase("Aluminum Can"))
                .mapToInt(RecyclingSubmission::getQuantity)
                .sum();

        List<BadgeDto> badges = new ArrayList<>();

        // 🎖️ Badge 1: Eco Pioneer
        badges.add(new BadgeDto(
                "eco_pioneer", "Eco Pioneer", "Completed your first recycling scan on campus!", "leaf",
                !submissions.isEmpty()
        ));

        // 🎖️ Badge 2: Plastic Ninja
        badges.add(new BadgeDto(
                "plastic_ninja", "Plastic Ninja", "Successfully recycled 20+ plastic bottles.", "water",
                totalPlastics >= 20
        ));

        // 🎖️ Badge 3: Metal Master
        badges.add(new BadgeDto(
                "metal_master", "Metal Master", "Recycled 15+ aluminum beverage cans.", "trophy",
                totalMetals >= 15
        ));

        // 🎖️ Badge 4: Elite Recycler
        badges.add(new BadgeDto(
                "century_club", "Elite Recycler", "Accumulated over 500 total points.", "star",
                currentPoints >= 500
        ));

        return badges;
    }
}