package com.campusrecycle.controller;

import com.campusrecycle.dto.CampusAnalyticsDto;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.repository.RecyclingSubmissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final RecyclingSubmissionRepository submissionRepository;

    public AnalyticsController(RecyclingSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    // 📈 GET /analytics/campus
    @GetMapping("/campus")
    public ResponseEntity<CampusAnalyticsDto> getCampusStats() {
        List<RecyclingSubmission> allSubmissions = submissionRepository.findAll();

        long plasticCount = allSubmissions.stream()
                .filter(s -> s.getItemType().equalsIgnoreCase("Plastic Bottle"))
                .mapToLong(RecyclingSubmission::getQuantity).sum();

        long metalCount = allSubmissions.stream()
                .filter(s -> s.getItemType().equalsIgnoreCase("Aluminum Can"))
                .mapToLong(RecyclingSubmission::getQuantity).sum();

        long totalItems = plasticCount + metalCount;
        long totalPoints = allSubmissions.stream().mapToLong(RecyclingSubmission::getPointsEarned).sum();

        // 🍃 Formula metrics: ~0.05kg CO2 saved per bottle, ~0.09kg saved per metal can
        double calculatedCo2Saved = (plasticCount * 0.05) + (metalCount * 0.09);

        // Rounding up nicely to 2 decimals
        calculatedCo2Saved = Math.round(calculatedCo2Saved * 100.0) / 100.0;

        return ResponseEntity.ok(new CampusAnalyticsDto(
                totalItems, plasticCount, metalCount, totalPoints, calculatedCo2Saved
        ));
    }
}