package com.campusrecycle.dto;

public record CampusAnalyticsDto(
        long totalItemsRecycled,
        long totalPlasticBottles,
        long totalAluminumCans,
        long totalPointsDistributed,
        double co2SavedKg
) {}