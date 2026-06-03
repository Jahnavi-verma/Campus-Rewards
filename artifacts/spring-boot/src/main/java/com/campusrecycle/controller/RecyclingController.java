package com.campusrecycle.controller;

import com.campusrecycle.dto.SubmissionDto;
import com.campusrecycle.dto.SubmissionRequest;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.service.RecyclingSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recycling")
@CrossOrigin(origins = "*") // 🌐 Enables painless connections from Expo Go mobile apps
public class RecyclingController {

    private final RecyclingSubmissionService submissionService;

    public RecyclingController(RecyclingSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * 📱 NEW: POST /recycling/scan-qr
     * Triggered automatically after scanning the hardware smart-bin's monitor QR code.
     * Expects body format: { "sessionId": "session_1780465540860" }
     */
    @PostMapping("/scan-qr")
    public ResponseEntity<Map<String, String>> claimQrSession(Authentication authentication, 
                                                              @RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(authentication.getName());
        String sessionId = body.get("sessionId");

        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "sessionId is required", "status", "ERROR"));
        }

        try {
            String successMsg = submissionService.processQrClaim(userId, sessionId);
            return ResponseEntity.ok(Map.of("message", successMsg, "status", "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "status", "ERROR"));
        }
    }

    // --- Legacy Framework Methods Intact ---

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody SubmissionRequest request,
                                    Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            RecyclingSubmission saved = submissionService.submit(userId, request);
            return ResponseEntity.ok(SubmissionDto.from(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubmissionDto>> mySubmissions(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<SubmissionDto> list = submissionService.getUserSubmissions(userId)
                .stream().map(SubmissionDto::from).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> itemInfo() {
        return ResponseEntity.ok(submissionService.getItemInfo());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(submissionService.getCampusStats());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SubmissionDto>> recent() {
        List<SubmissionDto> list = submissionService.getRecentSubmissions()
                .stream().map(SubmissionDto::from).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubmissionDto>> allSubmissions() {
        List<SubmissionDto> list = submissionService.getAllSubmissions()
                .stream().map(SubmissionDto::from).toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<?> review(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    Authentication authentication) {
        String status = body.get("status");
        if (status == null) return ResponseEntity.badRequest().body(Map.of("error", "status is required"));
        Long reviewerId = Long.parseLong(authentication.getName());
        RecyclingSubmission updated = submissionService.review(id, status, reviewerId);
        return ResponseEntity.ok(SubmissionDto.from(updated));
    }
}