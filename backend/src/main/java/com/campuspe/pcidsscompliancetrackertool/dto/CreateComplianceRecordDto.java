package com.campuspe.pcidsscompliancetrackertool.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new compliance record.
 *
 * <p>Fields marked as {@code @NotBlank} are mandatory; all other fields are
 * optional and default to {@code null} when omitted.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplianceRecordDto {

    @NotBlank(message = "Requirement ID is required")
    @Size(max = 50, message = "Requirement ID must not exceed 50 characters")
    private String requirementId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotBlank(message = "Status is required")
    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;

    @DecimalMin(value = "0.00", message = "Compliance score must be at least 0.00")
    @DecimalMax(value = "100.00", message = "Compliance score must not exceed 100.00")
    @Digits(integer = 3, fraction = 2,
            message = "Compliance score must have at most 3 integer and 2 fractional digits")
    private BigDecimal complianceScore;

    @Size(max = 150, message = "AssignedTo must not exceed 150 characters")
    private String assignedTo;

    private LocalDate dueDate;

    private LocalDate reviewDate;

    private String evidenceNotes;

    private String aiDescription;

    private String aiRecommendations;
}
