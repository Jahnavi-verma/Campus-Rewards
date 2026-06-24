package com.campusrecycle.service;

import com.campusrecycle.repository.HeatmapSubmissionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HeatmapService {

    private final HeatmapSubmissionRepository heatmapRepository;

    public HeatmapService(HeatmapSubmissionRepository heatmapRepository) {
        this.heatmapRepository = heatmapRepository;
    }

    public List<String> fetchUserHeatmapDates(Long userId) {
        System.out.println("📊 [ISOLATED HEATMAP ENGINE] Fetching unique activity days for user: " + userId);
        return heatmapRepository.findUniqueActivityDatesByUserId(userId);
    }
}