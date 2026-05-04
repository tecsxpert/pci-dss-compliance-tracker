package com.campuspe.pcidsscompliancetrackertool.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper DTO for paginated API responses.
 *
 * <p>Provides a standardised envelope that decouples client code from
 * Spring Data's {@link Page} contract while still exposing all the
 * pagination metadata needed for navigation controls.</p>
 *
 * @param <T> the type of elements in {@code content}
 */
public class PagedResponseDto<T> {

    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean isFirst;
    private boolean isLast;

    public PagedResponseDto() {
    }

    public PagedResponseDto(List<T> content, int currentPage, int totalPages,
                            long totalElements, int pageSize,
                            boolean isFirst, boolean isLast) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.isFirst = isFirst;
        this.isLast = isLast;
    }

    // ── Static factory ───────────────────────────────────────────────────────

    /**
     * Creates a {@code PagedResponseDto} directly from a Spring Data {@link Page}.
     *
     * @param page the Spring Data page to convert
     * @param <T>  the element type
     * @return a fully populated {@code PagedResponseDto}
     */
    public static <T> PagedResponseDto<T> fromPage(Page<T> page) {
        return new PagedResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }

    // ── Getters & setters ────────────────────────────────────────────────────

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
