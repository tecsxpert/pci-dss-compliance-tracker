package com.campuspe.pcidsscompliancetrackertool;

import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceRecordResponseDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.ComplianceUpdateRequestDTO;
import com.campuspe.pcidsscompliancetrackertool.dto.CreateComplianceRecordDto;
import com.campuspe.pcidsscompliancetrackertool.entity.AuditLog;
import com.campuspe.pcidsscompliancetrackertool.repository.AuditLogRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * Integration tests that verify an {@link AuditLog} entry is written to the
 * {@code audit_log} table after every CUD (Create / Update / Delete) operation
 * on {@code ComplianceRecord}.
 *
 * <p>Like {@link ComplianceRecordIntegrationTest} this class spins up real
 * PostgreSQL 15 and Redis 7 containers; the same Flyway migrations run
 * automatically before the first test.</p>
 *
 * <p>Audit entries are queried directly via the autowired
 * {@link AuditLogRepository} — no mocking involved.</p>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@Import(IntegrationTestConfig.class)
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditLogIntegrationTest {

    // ── Container image constants ──────────────────────────────────────────────

    static final String POSTGRES_IMAGE = "postgres:15";
    static final String REDIS_IMAGE    = "redis:7";

    // ── Static test-data constants ────────────────────────────────────────────

    static final String AUDIT_REQ_ID         = "Req-AUDIT-001";
    static final String AUDIT_TITLE          = "Audit Test – Network Segmentation";
    static final String AUDIT_DESCRIPTION    = "Verify cardholder data environment is isolated.";
    static final String AUDIT_STATUS_INITIAL = "NOT_STARTED";
    static final String AUDIT_STATUS_UPDATED = "IN_PROGRESS";
    static final String AUDIT_TITLE_UPDATED  = "Audit Test – Network Segmentation (In Progress)";
    static final String AUDIT_ASSIGNED_TO    = "network-team@pcidss.test";
    static final BigDecimal AUDIT_SCORE      = new BigDecimal("50.00");
    static final LocalDate  AUDIT_DUE_DATE   = LocalDate.now().plusDays(60);

    /** Entity type string stored in audit_log by AuditAspect. */
    static final String ENTITY_TYPE = "ComplianceRecord";

    /** Username embedded in the JWT used for CREATE / UPDATE / DELETE calls. */
    static final String ADMIN_USERNAME = "admin_user";

    static final CreateComplianceRecordDto AUDIT_CREATE_REQUEST = CreateComplianceRecordDto.builder()
            .requirementId(AUDIT_REQ_ID)
            .title(AUDIT_TITLE)
            .description(AUDIT_DESCRIPTION)
            .status(AUDIT_STATUS_INITIAL)
            .complianceScore(AUDIT_SCORE)
            .assignedTo(AUDIT_ASSIGNED_TO)
            .dueDate(AUDIT_DUE_DATE)
            .build();

    // ── Testcontainers containers ─────────────────────────────────────────────

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                    .withDatabaseName("pci_dss_audit_it_db")
                    .withUsername("audit_it_user")
                    .withPassword("audit_it_pass");

    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                    .withExposedPorts(6379);

    /*
     * CRITICAL: Start containers in a static initializer so they are running
     * BEFORE SpringExtension.BeforeAllCallback loads the application context.
     * Without this, EmbeddedDatabaseCondition calls POSTGRES::getJdbcUrl on a
     * not-yet-started container, which triggers and permanently caches a Docker
     * detection failure for the entire JVM lifetime.
     */
    static {
        POSTGRES.start();
        REDIS.start();
    }

    // ── Dynamic property overrides ────────────────────────────────────────────

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",  POSTGRES::getUsername);
        registry.add("spring.datasource.password",  POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");

        // Redis
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
        registry.add("spring.autoconfigure.exclude", () -> "");
    }

    // ── Injected components ───────────────────────────────────────────────────

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // ── Shared state between ordered tests ───────────────────────────────────

    private UUID auditedRecordId;
    private String adminToken;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeAll
    void setUp() {
        adminToken = "Bearer " + jwtUtil.generateToken(ADMIN_USERNAME, List.of("ADMIN"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, adminToken);
        return headers;
    }

    /**
     * Polls the audit_log repository for an entry matching the given entity id
     * and action. Retries up to 5 times with a 200 ms pause because the AuditAspect
     * writes the log synchronously within the same transaction — but the test
     * sometimes observes the write slightly after the HTTP response returns.
     */
    private List<AuditLog> findAuditLogs(String entityId, String action) {
        for (int attempt = 0; attempt < 5; attempt++) {
            List<AuditLog> logs = auditLogRepository
                    .findByEntityTypeAndEntityIdOrderByChangedAtDesc(ENTITY_TYPE, entityId);
            List<AuditLog> filtered = logs.stream()
                    .filter(l -> action.equals(l.getAction()))
                    .toList();
            if (!filtered.isEmpty()) return filtered;
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        return List.of();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test A — CREATE audit entry
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void create_shouldWriteCreateAuditLogEntry() {
        // Perform the CREATE via HTTP
        HttpEntity<CreateComplianceRecordDto> request =
                new HttpEntity<>(AUDIT_CREATE_REQUEST, authHeaders());

        ResponseEntity<ComplianceRecordResponseDTO> response = restTemplate.exchange(
                "/api/v1/compliance-records",
                HttpMethod.POST,
                request,
                ComplianceRecordResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        auditedRecordId = response.getBody().getId();

        // Verify audit entry
        List<AuditLog> logs = findAuditLogs(auditedRecordId.toString(), "CREATE");
        assertThat(logs)
                .as("Exactly one CREATE audit log entry should exist after creation")
                .isNotEmpty();

        AuditLog log = logs.get(0);
        assertThat(log.getEntityType()).isEqualTo(ENTITY_TYPE);
        assertThat(log.getEntityId()).isEqualTo(auditedRecordId.toString());
        assertThat(log.getAction()).isEqualTo("CREATE");
        assertThat(log.getChangedBy()).isEqualTo(ADMIN_USERNAME);
        assertThat(log.getChangedAt()).isNotNull();

        // On CREATE: oldValue is null, newValue contains the serialised entity
        assertThat(log.getOldValue()).isNull();
        assertThat(log.getNewValue()).isNotBlank();

        // newValue should contain the record's id and title
        assertThat(log.getNewValue()).contains(auditedRecordId.toString());
        assertThat(log.getNewValue()).contains(AUDIT_TITLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test B — UPDATE audit entry
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(2)
    void update_shouldWriteUpdateAuditLogEntry() {
        assertThat(auditedRecordId).as("Create test must run first").isNotNull();

        ComplianceUpdateRequestDTO updateDto = ComplianceUpdateRequestDTO.builder()
                .title(AUDIT_TITLE_UPDATED)
                .status(AUDIT_STATUS_UPDATED)
                .build();

        HttpEntity<ComplianceUpdateRequestDTO> request =
                new HttpEntity<>(updateDto, authHeaders());

        ResponseEntity<ComplianceRecordResponseDTO> response = restTemplate.exchange(
                "/api/v1/compliance-records/" + auditedRecordId,
                HttpMethod.PUT,
                request,
                ComplianceRecordResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify audit entry
        List<AuditLog> logs = findAuditLogs(auditedRecordId.toString(), "UPDATE");
        assertThat(logs)
                .as("At least one UPDATE audit log entry should exist after update")
                .isNotEmpty();

        AuditLog log = logs.get(0);
        assertThat(log.getEntityType()).isEqualTo(ENTITY_TYPE);
        assertThat(log.getEntityId()).isEqualTo(auditedRecordId.toString());
        assertThat(log.getAction()).isEqualTo("UPDATE");
        assertThat(log.getChangedBy()).isEqualTo(ADMIN_USERNAME);
        assertThat(log.getChangedAt()).isNotNull();

        // On UPDATE: both oldValue and newValue must be populated
        assertThat(log.getOldValue()).isNotBlank();
        assertThat(log.getNewValue()).isNotBlank();

        // newValue must reflect the updated title
        assertThat(log.getNewValue()).contains(AUDIT_TITLE_UPDATED);
        assertThat(log.getNewValue()).contains(AUDIT_STATUS_UPDATED);

        // oldValue must contain the original title (before update)
        assertThat(log.getOldValue()).contains(AUDIT_TITLE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test C — DELETE (soft) audit entry
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    void softDelete_shouldWriteDeleteAuditLogEntry() {
        assertThat(auditedRecordId).as("Create test must run first").isNotNull();

        HttpEntity<Void> request = new HttpEntity<>(authHeaders());

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/compliance-records/" + auditedRecordId,
                HttpMethod.DELETE,
                request,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify audit entry
        List<AuditLog> logs = findAuditLogs(auditedRecordId.toString(), "DELETE");
        assertThat(logs)
                .as("Exactly one DELETE audit log entry should exist after soft-delete")
                .isNotEmpty();

        AuditLog log = logs.get(0);
        assertThat(log.getEntityType()).isEqualTo(ENTITY_TYPE);
        assertThat(log.getEntityId()).isEqualTo(auditedRecordId.toString());
        assertThat(log.getAction()).isEqualTo("DELETE");
        assertThat(log.getChangedBy()).isEqualTo(ADMIN_USERNAME);
        assertThat(log.getChangedAt()).isNotNull();

        // On DELETE: oldValue contains last known state; newValue is null
        assertThat(log.getOldValue()).isNotBlank();
        assertThat(log.getNewValue()).isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test D — Verify complete audit trail for the record
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    void auditTrail_shouldContainAllThreeCudActions() {
        assertThat(auditedRecordId).as("Previous tests must run first").isNotNull();

        List<AuditLog> allLogs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByChangedAtDesc(ENTITY_TYPE, auditedRecordId.toString());

        assertThat(allLogs).hasSizeGreaterThanOrEqualTo(3);

        // All entries must have the required fields populated
        for (AuditLog log : allLogs) {
            assertThat(log.getEntityType()).isNotBlank();
            assertThat(log.getEntityId()).isNotBlank();
            assertThat(log.getAction()).isNotBlank();
            assertThat(log.getChangedBy()).isNotBlank();
            assertThat(log.getChangedAt()).isNotNull();
        }

        // The three expected actions must all be present
        List<String> actions = allLogs.stream().map(AuditLog::getAction).toList();
        assertThat(actions).contains("CREATE", "UPDATE", "DELETE");
    }
}
