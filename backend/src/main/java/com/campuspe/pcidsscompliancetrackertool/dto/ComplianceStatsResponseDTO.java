package com.campuspe.pcidsscompliancetrackertool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceStatsResponseDTO {

    private long totalRecords;

    private Map<String, Long> countByStatus;

    private BigDecimal averageComplianceScore;

    private long overdueCount;
}
