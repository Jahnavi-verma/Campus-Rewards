package com.campusrecycle.dto;

import com.campusrecycle.model.RecyclingSubmission;

import java.time.LocalDateTime;

public class SubmissionDto {

    private Long id;
    private Long userId;
    private String userName;
    private String itemType;
    private int quantity;
    private int pointsEarned;
    private String location;
    private String notes;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public static SubmissionDto from(RecyclingSubmission s) {
        SubmissionDto dto = new SubmissionDto();
        dto.id = s.getId();
        dto.userId = s.getUser().getId();
        dto.userName = s.getUser().getName();
        dto.itemType = s.getItemType();
        dto.quantity = s.getQuantity();
        dto.pointsEarned = s.getPointsEarned();
        dto.location = s.getLocation();
        dto.notes = s.getNotes();
        dto.status = s.getStatus();
        dto.submittedAt = s.getSubmittedAt();
        dto.reviewedAt = s.getReviewedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getItemType() { return itemType; }
    public int getQuantity() { return quantity; }
    public int getPointsEarned() { return pointsEarned; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
