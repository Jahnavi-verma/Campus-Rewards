package com.campusrecycle.controller;

import com.campusrecycle.dto.HeatmapResponse;
import com.campusrecycle.service.HeatmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard-heatmap") // Distinct route endpoint path
@CrossOrigin(origins = "*") 
public class HeatmapController {

    private final HeatmapService heatmapService;

    public HeatmapController(HeatmapService heatmapService) {
        this.heatmapService = heatmapService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserHeatmap(@PathVariable Long userId) {
        try {
            List<String> activityDates = heatmapService.fetchUserHeatmapDates(userId);
            return ResponseEntity.ok(new HeatmapResponse(userId, activityDates));
        } catch (Exception e) {
            System.err.println("💥 Heatmap fetch error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
