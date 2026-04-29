package com.campuspe.pcidsscompliancetrackertool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRecordResponseDTO {

    private UUID id;

    private String requirementId;

    private String title;

    private String description;

    private String status;

    private BigDecimal complianceScore;

    private String assignedTo;

    private LocalDate dueDate;

    private LocalDate reviewDate;

    private String evidenceNotes;

    private String aiDescription;

    private String aiRecommendations;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
