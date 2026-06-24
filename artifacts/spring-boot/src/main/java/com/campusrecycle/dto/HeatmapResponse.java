package com.campusrecycle.dto;

import java.util.List;

public class HeatmapResponse {
    private Long userId;
    private int activeDaysCount;
    private List<String> dates;

    public HeatmapResponse(Long userId, List<String> dates) {
        this.userId = userId;
        this.dates = dates;
        this.activeDaysCount = dates != null ? dates.size() : 0;
    }

    // Getters
    public Long getUserId() { return userId; }
    public int getActiveDaysCount() { return activeDaysCount; }
    public List<String> getDates() { return dates; }
}