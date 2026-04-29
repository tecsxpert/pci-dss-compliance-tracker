package com.campuspe.pcidsscompliancetrackertool.service;

import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceRecordResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceStatsResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceUpdateRequestDTO;
import com.campuspe.pcidsscompliancetrackertool.entity.ComplianceRecord;
import com.campuspe.pcidsscompliancetrackertool.exception.ResourceNotFoundException;
import com.campuspe.pcidsscompliancetrackertool.repository.ComplianceRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplianceRecordService {

    private static final String COMPLIANT_STATUS = "COMPLIANT";

    private final ComplianceRecordRepository repository;

    public ComplianceRecordService(ComplianceRecordRepository repository) {
        this.repository = repository;
    }

    public ComplianceRecordResponseDTO toResponseDTO(ComplianceRecord entity) {
        return ComplianceRecordResponseDTO.builder()
                .id(entity.getId())
                .requirementId(entity.getRequirementId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .complianceScore(entity.getComplianceScore())
                .assignedTo(entity.getAssignedTo())
                .dueDate(entity.getDueDate())
                .reviewDate(entity.getReviewDate())
                .evidenceNotes(entity.getEvidenceNotes())
                .aiDescription(entity.getAiDescription())
                .aiRecommendations(entity.getAiRecommendations())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Transactional
    public ComplianceRecordResponseDTO updateRecord(UUID id, ComplianceUpdateRequestDTO dto) {
        ComplianceRecord record = repository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", "id", id));

        if (dto.getRequirementId() != null) {
            record.setRequirementId(dto.getRequirementId());
        }
        if (dto.getTitle() != null) {
            record.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            record.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            record.setStatus(dto.getStatus());
        }
        if (dto.getComplianceScore() != null) {
            record.setComplianceScore(dto.getComplianceScore());
        }
        if (dto.getAssignedTo() != null) {
            record.setAssignedTo(dto.getAssignedTo());
        }
        if (dto.getDueDate() != null) {
            record.setDueDate(dto.getDueDate());
        }
        if (dto.getReviewDate() != null) {
            record.setReviewDate(dto.getReviewDate());
        }
        if (dto.getEvidenceNotes() != null) {
            record.setEvidenceNotes(dto.getEvidenceNotes());
        }
        if (dto.getAiDescription() != null) {
            record.setAiDescription(dto.getAiDescription());
        }
        if (dto.getAiRecommendations() != null) {
            record.setAiRecommendations(dto.getAiRecommendations());
        }

        ComplianceRecord saved = repository.save(record);
        return toResponseDTO(saved);
    }

    @Transactional
    public void softDeleteRecord(UUID id) {
        ComplianceRecord record = repository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord", "id", id));

        record.setIsDeleted(true);
        repository.save(record);
    }

    public Page<ComplianceRecordResponseDTO> searchRecords(String keyword, Pageable pageable) {
        return repository.searchByKeywordPaginated(keyword, pageable)
                .map(this::toResponseDTO);
    }

    public ComplianceStatsResponseDTO getStatistics() {
        long totalRecords = repository.countActiveRecords();

        List<Object[]> statusCounts = repository.countByStatus();
        Map<String, Long> countByStatus = new LinkedHashMap<>();
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            long count = ((Number) row[1]).longValue();
            countByStatus.put(status, count);
        }

        BigDecimal avgScore = repository.findAverageComplianceScore();
        if (avgScore != null) {
            avgScore = avgScore.setScale(2, RoundingMode.HALF_UP);
        } else {
            avgScore = BigDecimal.ZERO;
        }

        long overdueCount = repository.countOverdueRecords(LocalDate.now(), COMPLIANT_STATUS);

        return ComplianceStatsResponseDTO.builder()
                .totalRecords(totalRecords)
                .countByStatus(countByStatus)
                .averageComplianceScore(avgScore)
                .overdueCount(overdueCount)
                .build();
    }
}
