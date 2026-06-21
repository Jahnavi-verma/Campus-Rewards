package com.campusrecycle.controller;

import com.campusrecycle.dto.QrClaimRequest;
import com.campusrecycle.dto.QrClaimResponse;
import com.campusrecycle.dto.SubmissionRequest;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.service.RecyclingSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recycling")
public class RecyclingController {

    private final RecyclingSubmissionService recyclingSubmissionService;

    public RecyclingController(RecyclingSubmissionService recyclingSubmissionService) {
        this.recyclingSubmissionService = recyclingSubmissionService;
    }

    /**
     * 🔒 GET /api/recycling/active-session
     * Handshake verification endpoint: Fetches current allowed live kiosk session ID.
     */
    @GetMapping("/active-session")
    public ResponseEntity<Map<String, String>> getActiveSession(Authentication authentication) {
        try {
            String activeSessionId = recyclingSubmissionService.getActiveSessionId().get();
            if (activeSessionId.isBlank()) {
                return ResponseEntity.status(404).body(Map.of("error", "No active session running on the kiosk."));
            }
            return ResponseEntity.ok(Map.of("activeSessionId", activeSessionId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve active configuration: " + e.getMessage()));
        }
    }

    /**
     * 🔒 POST /api/recycling/claim-qr
     * Final Transaction Sync: Moves processing logs to SQL and issues profile points.
     */
    @PostMapping("/claim-qr")
    public ResponseEntity<QrClaimResponse> claimQr(@RequestBody QrClaimRequest request, Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String message = recyclingSubmissionService.processQrClaim(userId, request.getSessionId());
            return ResponseEntity.ok(new QrClaimResponse("SUCCESS", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new QrClaimResponse("ERROR", e.getMessage()));
        }
    }
}