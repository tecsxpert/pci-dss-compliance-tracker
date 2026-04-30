package com.campuspe.pcidsscompliancetrackertool.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Reusable utility for building validated {@link Pageable} instances from
 * raw request parameters.
 *
 * <p>Centralises sort-field whitelisting and direction validation so that
 * every paginated endpoint enforces identical rules without duplicated
 * logic.</p>
 */
public final class PageableBuilder {

    private PageableBuilder() {
        // Utility class — prevent instantiation
    }

    /**
     * Sort fields that are safe to pass to JPA {@code ORDER BY}.
     * Any value not in this set is rejected to prevent injection attacks.
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "title",
            "status",
            "complianceScore",
            "dueDate",
            "reviewDate",
            "requirementId",
            "assignedTo"
    );

    /**
     * Builds a {@link Pageable} after validating the sort parameters.
     *
     * @param page    zero-based page index
     * @param size    number of elements per page
     * @param sortBy  field to sort by — must be present in {@link #ALLOWED_SORT_FIELDS}
     * @param sortDir sort direction — must be {@code "asc"} or {@code "desc"}
     * @return a validated {@link Pageable}
     * @throws IllegalArgumentException if {@code sortBy} or {@code sortDir} is invalid
     */
    public static Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        // Validate sort field against whitelist
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_FIELDS);
        }

        // Validate sort direction
        if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
            throw new IllegalArgumentException(
                    "Invalid sort direction: '" + sortDir + "'. Must be 'asc' or 'desc'.");
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(page, size, sort);
    }
}
