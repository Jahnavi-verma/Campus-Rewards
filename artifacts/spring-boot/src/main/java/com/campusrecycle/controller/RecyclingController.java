package com.campusrecycle.controller;

import com.campusrecycle.dto.SubmissionRequest;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.service.RecyclingSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.campusrecycle.dto.QrClaimRequest;
import com.campusrecycle.dto.QrClaimResponse;
import com.campusrecycle.dto.SubmissionDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recycling")
@CrossOrigin(origins = "*")
public class RecyclingController {

    private final RecyclingSubmissionService submissionService;

    public RecyclingController(RecyclingSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * 📱 POST /recycling/scan-qr
     * Triggered automatically after scanning the hardware smart-bin's monitor QR code.
     * Expects body format: { "sessionId": "session_1780465540860" }
     */
    @PostMapping("/scan-qr")
    public ResponseEntity<QrClaimResponse> claimQrSession(Authentication authentication, 
                                                          @RequestBody QrClaimRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        String sessionId = request.getSessionId();

        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body(new QrClaimResponse("ERROR", "sessionId is required"));
        }

        try {
            // Step 7 Logic Complete: Processes transaction values and returns standardized QrClaimResponse layout
            String successMsg = submissionService.processQrClaim(userId, sessionId);
            return ResponseEntity.ok(new QrClaimResponse("SUCCESS", successMsg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new QrClaimResponse("ERROR", e.getMessage()));
        }
    }

    // --- Legacy Endpoints Kept Completely Intact ---

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