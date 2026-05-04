package com.campuspe.pcidsscompliancetrackertool;

import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceRecordResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceStatsResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceUpdateRequestDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.CreateComplianceRecordDto;
import com.campuspe.pcidsscompliancetrackertool.dto.PagedResponseDto;
import com.campuspe.pcidsscompliancetrackertool.security.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack CRUD integration test for the Compliance Record API.
 *
 * <p>Spins up real PostgreSQL 15 and Redis 7 containers via Testcontainers,
 * runs Flyway migrations, and exercises every CRUD endpoint through
 * {@link TestRestTemplate} with a real JWT token. Tests run in a strict
 * sequential order so that each step builds on the previous one.</p>
 *
 * <p>The Spring context is shared across all test methods via
 * {@code @TestInstance(PER_CLASS)}, keeping container startup to a single
 * occurrence per test run.</p>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComplianceRecordIntegrationTest {

    // ── Container image constants ──────────────────────────────────────────────

    static final String POSTGRES_IMAGE = "postgres:15";
    static final String REDIS_IMAGE    = "redis:7";

    // ── Static test-data constants ────────────────────────────────────────────

    static final String REQ_ID         = "Req-IT-001";
    static final String TITLE          = "Integration Test – Encryption at Rest";
    static final String DESCRIPTION    = "Verify that cardholder data is encrypted at rest using AES-256.";
    static final String STATUS_INITIAL = "IN_PROGRESS";
    static final String STATUS_UPDATED = "COMPLIANT";
    static final String TITLE_UPDATED  = "Integration Test – Encryption at Rest (Verified)";
    static final String ASSIGNED_TO    = "security-team@pcidss.test";
    static final String EVIDENCE_NOTES = "Encryption policy doc reviewed and confirmed.";
    static final BigDecimal COMPLIANCE_SCORE = new BigDecimal("72.50");
    static final LocalDate  DUE_DATE         = LocalDate.now().plusDays(90);
    static final LocalDate  REVIEW_DATE      = LocalDate.now().plusDays(30);

    /**
     * Static DTO that is reused across all test steps.
     * Never hardcode inline strings inside test assertions — always reference
     * these constants.
     */
    static final CreateComplianceRecordDto CREATE_REQUEST = CreateComplianceRecordDto.builder()
            .requirementId(REQ_ID)
            .title(TITLE)
            .description(DESCRIPTION)
            .status(STATUS_INITIAL)
            .complianceScore(COMPLIANCE_SCORE)
            .assignedTo(ASSIGNED_TO)
            .dueDate(DUE_DATE)
            .reviewDate(REVIEW_DATE)
            .evidenceNotes(EVIDENCE_NOTES)
            .build();

    // ── Testcontainers containers ─────────────────────────────────────────────

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                    .withDatabaseName("pci_dss_it_db")
                    .withUsername("pci_it_user")
                    .withPassword("pci_it_pass");

    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                    .withExposedPorts(6379);

    /*
     * CRITICAL: Start containers in a static initializer so they are running
     * BEFORE any JUnit 5 extension fires its BeforeAllCallback.
     *
     * @SpringBootTest registers SpringExtension first (annotation order), so
     * SpringExtension.BeforeAllCallback loads the Spring context before
     * TestcontainersExtension.BeforeAllCallback starts the containers.
     * When EmbeddedDatabaseCondition evaluates spring.datasource.url it calls
     * POSTGRES::getJdbcUrl() on a not-yet-started container, which triggers
     * Testcontainers Docker detection and caches a failure permanently for that
     * JVM. Starting the containers here (class-load time) avoids this race.
     */
    static {
        POSTGRES.start();
        REDIS.start();
    }

    // ── Dynamic property overrides ────────────────────────────────────────────

    /**
     * Overrides the Spring datasource and Redis properties at context-startup
     * time with the real container connection details. This runs once before
     * the Spring context is loaded.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",  POSTGRES::getUsername);
        registry.add("spring.datasource.password",  POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Hibernate dialect must match Postgres
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");

        // Redis
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");

        // Re-enable Redis auto-configuration (integration profile disables it by default)
        registry.add("spring.autoconfigure.exclude", () -> "");
    }

    // ── Injected Spring components ────────────────────────────────────────────

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── Shared state between ordered tests ───────────────────────────────────

    /** Populated in Test 1; consumed by Tests 2-8. */
    private UUID createdRecordId;

    /** Bearer token generated once in @BeforeAll. */
    private String adminToken;
    private String managerToken;
    private String viewerToken;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeAll
    void setUpTokens() {
        // Generate real JWT tokens — the secret matches the integration-test profile
        adminToken   = "Bearer " + jwtUtil.generateToken("admin_user",   List.of("ADMIN"));
        managerToken = "Bearer " + jwtUtil.generateToken("manager_user", List.of("MANAGER"));
        viewerToken  = "Bearer " + jwtUtil.generateToken("viewer_user",  List.of("VIEWER"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private HttpHeaders authHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        return headers;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pre-flight: verify Flyway ran all migrations
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(0)
    void flywayMigrations_shouldHaveCreatedAllTables() {
        // compliance_records — created by V1
        Integer crCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'compliance_records'",
                Integer.class);
        assertThat(crCount).isEqualTo(1);

        // audit_log — created by V2
        Integer alCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'audit_log'",
                Integer.class);
        assertThat(alCount).isEqualTo(1);

        // users — created by V3
        Integer usCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'users'",
                Integer.class);
        assertThat(usCount).isEqualTo(1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1 — POST /api/v1/compliance-records  (create)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void createRecord_shouldReturn201AndPersistRecord() {
        HttpEntity<CreateComplianceRecordDto> request =
                new HttpEntity<>(CREATE_REQUEST, authHeaders(adminToken));

        ResponseEntity<ComplianceRecordResponseDTO> response = restTemplate.exchange(
                "/api/v1/compliance-records",
                HttpMethod.POST,
                request,
                ComplianceRecordResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ComplianceRecordResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isNotNull();
        assertThat(body.getRequirementId()).isEqualTo(REQ_ID);
        assertThat(body.getTitle()).isEqualTo(TITLE);
        assertThat(body.getStatus()).isEqualTo(STATUS_INITIAL);
        assertThat(body.getAssignedTo()).isEqualTo(ASSIGNED_TO);
        assertThat(body.getComplianceScore()).isEqualByComparingTo(COMPLIANCE_SCORE);
        assertThat(body.getCreatedAt()).isNotNull();

        // Save for subsequent tests
        createdRecordId = body.getId();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2 — GET /api/v1/compliance-records/{id}  (read)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(2)
    void getRecordById_shouldReturnAllFieldsMatchingCreateRequest() {
        assertThat(createdRecordId).as("createRecord test must run first").isNotNull();

        HttpEntity<Void> request = new HttpEntity<>(authHeaders(viewerToken));

        ResponseEntity<ComplianceRecordResponseDTO> response = restTemplate.exchange(
                "/api/v1/compliance-records/" + createdRecordId,
                HttpMethod.GET,
                request,
                ComplianceRecordResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ComplianceRecordResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(createdRecordId);
        assertThat(body.getRequirementId()).isEqualTo(REQ_ID);
        assertThat(body.getTitle()).isEqualTo(TITLE);
        assertThat(body.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(body.getStatus()).isEqualTo(STATUS_INITIAL);
        assertThat(body.getComplianceScore()).isEqualByComparingTo(COMPLIANCE_SCORE);
        assertThat(body.getAssignedTo()).isEqualTo(ASSIGNED_TO);
        assertThat(body.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(body.getReviewDate()).isEqualTo(REVIEW_DATE);
        assertThat(body.getEvidenceNotes()).isEqualTo(EVIDENCE_NOTES);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3 — PUT /api/v1/compliance-records/{id}  (update)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    void updateRecord_shouldReturn200WithUpdatedValues() {
        assertThat(createdRecordId).as("createRecord test must run first").isNotNull();

        ComplianceUpdateRequestDTO updateDto = ComplianceUpdateRequestDTO.builder()
                .title(TITLE_UPDATED)
                .status(STATUS_UPDATED)
                .complianceScore(new BigDecimal("95.00"))
                .build();

        HttpEntity<ComplianceUpdateRequestDTO> request =
                new HttpEntity<>(updateDto, authHeaders(adminToken));

        ResponseEntity<ComplianceRecordResponseDTO> response = restTemplate.exchange(
                "/api/v1/compliance-records/" + createdRecordId,
                HttpMethod.PUT,
                request,
                ComplianceRecordResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ComplianceRecordResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo(TITLE_UPDATED);
        assertThat(body.getStatus()).isEqualTo(STATUS_UPDATED);
        assertThat(body.getComplianceScore()).isEqualByComparingTo(new BigDecimal("95.00"));
        // Fields not in the update DTO should remain unchanged
        assertThat(body.getRequirementId()).isEqualTo(REQ_ID);
        assertThat(body.getAssignedTo()).isEqualTo(ASSIGNED_TO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4 — GET /api/v1/compliance-records/search?q={keyword}  (search)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    void searchRecords_shouldFindUpdatedRecordByKeyword() {
        assertThat(createdRecordId).as("createRecord test must run first").isNotNull();

        // Use part of the updated title as the search keyword
        String keyword = "Encryption at Rest";

        HttpEntity<Void> request = new HttpEntity<>(authHeaders(viewerToken));

        ResponseEntity<PagedResponseDto<ComplianceRecordResponseDTO>> response = restTemplate.exchange(
                "/api/v1/compliance-records/search?q=" + keyword,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PagedResponseDto<ComplianceRecordResponseDTO>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        PagedResponseDto<ComplianceRecordResponseDTO> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getContent()).isNotEmpty();

        // The updated record must appear in the results
        boolean found = body.getContent().stream()
                .anyMatch(r -> createdRecordId.equals(r.getId()));
        assertThat(found).as("Updated record should appear in search results").isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 5 — GET /api/v1/compliance-records/stats  (statistics)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void getStats_shouldReturnAtLeastOneRecordAndNonNullStatusCounts() {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(adminToken));

        ResponseEntity<ComplianceStatsResponseDTO> response = restTemplate.exchange(
                "/api/v1/compliance-records/stats",
                HttpMethod.GET,
                request,
                ComplianceStatsResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ComplianceStatsResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalRecords()).isGreaterThanOrEqualTo(1L);
        assertThat(body.getCountByStatus()).isNotNull();
        // The COMPLIANT status we just set must appear
        assertThat(body.getCountByStatus()).containsKey(STATUS_UPDATED);
        assertThat(body.getCountByStatus().get(STATUS_UPDATED)).isGreaterThanOrEqualTo(1L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 6 — GET /api/v1/compliance-records/export/csv  (CSV export)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    void exportCsv_shouldReturn200WithTextCsvContentType() {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(adminToken));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/compliance-records/export/csv",
                HttpMethod.GET,
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Content-Type must be text/csv
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString())
                .startsWith("text/csv");

        // Body must not be empty and should contain the CSV header row
        String body = response.getBody();
        assertThat(body).isNotBlank();
        assertThat(body).contains("ID");
        assertThat(body).contains("Title");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 7 — DELETE /api/v1/compliance-records/{id}  (soft-delete)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    void deleteRecord_shouldReturn204() {
        assertThat(createdRecordId).as("createRecord test must run first").isNotNull();

        HttpEntity<Void> request = new HttpEntity<>(authHeaders(adminToken));

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/compliance-records/" + createdRecordId,
                HttpMethod.DELETE,
                request,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Confirm the row is flagged as deleted at the DB level
        Boolean isDeleted = jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM compliance_records WHERE id = ?::uuid",
                Boolean.class,
                createdRecordId.toString());
        assertThat(isDeleted).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 8 — GET /api/v1/compliance-records/{id}  (expect 404 after delete)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    void getRecordById_afterSoftDelete_shouldReturn404() {
        assertThat(createdRecordId).as("deleteRecord test must run first").isNotNull();

        HttpEntity<Void> request = new HttpEntity<>(authHeaders(viewerToken));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/compliance-records/" + createdRecordId,
                HttpMethod.GET,
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
