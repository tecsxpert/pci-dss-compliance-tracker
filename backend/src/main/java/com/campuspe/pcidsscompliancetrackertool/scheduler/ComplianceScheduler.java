package com.campuspe.pcidsscompliancetrackertool.scheduler;

import com.campuspe.pcidsscompliancetrackertool.entity.ComplianceRecord;
import com.campuspe.pcidsscompliancetrackertool.repository.ComplianceRecordRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduled jobs for PCI-DSS compliance email notifications.
 *
 * <ul>
 *   <li><strong>Job 1</strong> — Daily overdue reminder (8:00 AM)</li>
 *   <li><strong>Job 2</strong> — 7-day advance deadline alert (9:00 AM)</li>
 *   <li><strong>Job 3</strong> — Weekly summary report (Monday 7:00 AM)</li>
 * </ul>
 *
 * <p>All cron expressions and recipient addresses are injected from
 * {@code application.yml} — nothing is hardcoded.</p>
 */
@Component
public class ComplianceScheduler {

    private static final Logger log = LoggerFactory.getLogger(ComplianceScheduler.class);
    private static final String COMPLIANT_STATUS = "COMPLIANT";

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final ComplianceRecordRepository repository;
    private final JavaMailSender              mailSender;
    private final TemplateEngine              templateEngine;

    // ── Configuration (injected from application.yml) ─────────────────────────

    @Value("${scheduler.admin-email}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Constructs the scheduler with required dependencies.
     *
     * @param repository     compliance-record repository
     * @param mailSender     configured JavaMailSender
     * @param templateEngine Thymeleaf template engine for HTML emails
     */
    public ComplianceScheduler(ComplianceRecordRepository repository,
                               JavaMailSender mailSender,
                               TemplateEngine templateEngine) {
        this.repository     = repository;
        this.mailSender     = mailSender;
        this.templateEngine = templateEngine;
    }

    // ── Job 1: Daily Overdue Reminder (8:00 AM) ──────────────────────────────

    /**
     * Finds all overdue, non-compliant, non-deleted compliance records and sends
     * a reminder email to each record's {@code assignedTo} address.
     */
    @Async
    @Scheduled(cron = "${scheduler.cron.overdue-reminder}")
    public void sendOverdueReminders() {
        try {
            log.info("[Scheduler] Starting daily overdue-reminder job");
            LocalDate today = LocalDate.now();
            List<ComplianceRecord> overdueRecords =
                    repository.findOverdueRecords(today, COMPLIANT_STATUS);

            log.info("[Scheduler] Found {} overdue records", overdueRecords.size());

            for (ComplianceRecord record : overdueRecords) {
                if (record.getAssignedTo() == null || record.getAssignedTo().isBlank()) {
                    log.warn("[Scheduler] Skipping record {} — no assignedTo email", record.getId());
                    continue;
                }
                try {
                    Context ctx = new Context();
                    ctx.setVariable("title", record.getTitle());
                    ctx.setVariable("dueDate", record.getDueDate());
                    ctx.setVariable("status", record.getStatus());
                    ctx.setVariable("complianceScore", record.getComplianceScore());
                    ctx.setVariable("requirementId", record.getRequirementId());

                    String html = templateEngine.process("overdue-reminder", ctx);
                    sendHtmlEmail(record.getAssignedTo(),
                            "⚠️ Overdue: " + record.getTitle(), html);

                    log.info("[Scheduler] Sent overdue reminder for record {} to {}",
                            record.getId(), record.getAssignedTo());
                } catch (Exception e) {
                    log.error("[Scheduler] Failed to send overdue reminder for record {}: {}",
                            record.getId(), e.getMessage(), e);
                }
            }
            log.info("[Scheduler] Completed daily overdue-reminder job");
        } catch (Exception e) {
            log.error("[Scheduler] Overdue-reminder job failed: {}", e.getMessage(), e);
        }
    }

    // ── Job 2: 7-Day Advance Deadline Alert (9:00 AM) ────────────────────────

    /**
     * Finds all non-compliant records whose deadline falls within the next 7 days
     * and sends a deadline-approaching alert to each record's {@code assignedTo}.
     */
    @Async
    @Scheduled(cron = "${scheduler.cron.deadline-alert}")
    public void sendDeadlineAlerts() {
        try {
            log.info("[Scheduler] Starting 7-day deadline-alert job");
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysLater = today.plusDays(7);

            List<ComplianceRecord> upcomingRecords =
                    repository.findUpcomingDeadlineRecords(today, sevenDaysLater, COMPLIANT_STATUS);

            log.info("[Scheduler] Found {} records approaching deadline", upcomingRecords.size());

            for (ComplianceRecord record : upcomingRecords) {
                if (record.getAssignedTo() == null || record.getAssignedTo().isBlank()) {
                    log.warn("[Scheduler] Skipping record {} — no assignedTo email", record.getId());
                    continue;
                }
                try {
                    long daysRemaining = ChronoUnit.DAYS.between(today, record.getDueDate());

                    Context ctx = new Context();
                    ctx.setVariable("title", record.getTitle());
                    ctx.setVariable("daysRemaining", daysRemaining);
                    ctx.setVariable("dueDate", record.getDueDate());
                    ctx.setVariable("assignedTo", record.getAssignedTo());
                    ctx.setVariable("requirementId", record.getRequirementId());
                    ctx.setVariable("status", record.getStatus());

                    String html = templateEngine.process("deadline-alert", ctx);
                    sendHtmlEmail(record.getAssignedTo(),
                            "⏰ Deadline Approaching: " + record.getTitle(), html);

                    log.info("[Scheduler] Sent deadline alert for record {} to {}",
                            record.getId(), record.getAssignedTo());
                } catch (Exception e) {
                    log.error("[Scheduler] Failed to send deadline alert for record {}: {}",
                            record.getId(), e.getMessage(), e);
                }
            }
            log.info("[Scheduler] Completed 7-day deadline-alert job");
        } catch (Exception e) {
            log.error("[Scheduler] Deadline-alert job failed: {}", e.getMessage(), e);
        }
    }

    // ── Job 3: Weekly Summary Report (Monday 7:00 AM) ────────────────────────

    /**
     * Generates a weekly compliance summary (total records, per-status counts,
     * average compliance score, overdue count) and emails it to the configured
     * admin address.
     */
    @Async
    @Scheduled(cron = "${scheduler.cron.weekly-summary}")
    public void sendWeeklySummary() {
        try {
            log.info("[Scheduler] Starting weekly summary-report job");
            LocalDate today = LocalDate.now();

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

            long overdueCount = repository.countOverdueRecords(today, COMPLIANT_STATUS);

            Context ctx = new Context();
            ctx.setVariable("totalRecords", totalRecords);
            ctx.setVariable("countByStatus", countByStatus);
            ctx.setVariable("averageScore", avgScore);
            ctx.setVariable("overdueCount", overdueCount);
            ctx.setVariable("reportDate", today);

            String html = templateEngine.process("weekly-summary", ctx);
            sendHtmlEmail(adminEmail,
                    "📊 Weekly PCI-DSS Compliance Summary — " + today, html);

            log.info("[Scheduler] Weekly summary sent to {}", adminEmail);
        } catch (Exception e) {
            log.error("[Scheduler] Weekly summary job failed: {}", e.getMessage(), e);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Sends an HTML email via the configured {@link JavaMailSender}.
     *
     * @param to      recipient address
     * @param subject email subject line
     * @param html    rendered HTML body
     * @throws MessagingException if the message cannot be sent
     */
    private void sendHtmlEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
