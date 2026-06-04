package com.campusrecycle.dto;

public class QrClaimRequest {
    private String sessionId;

    // Default Constructor
    public QrClaimRequest() {}

    public QrClaimRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}