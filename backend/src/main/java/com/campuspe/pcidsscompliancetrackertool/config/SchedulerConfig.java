package com.campuspe.pcidsscompliancetrackertool.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's {@code @Scheduled} and {@code @Async} support.
 *
 * <p>Placed in a dedicated configuration class rather than on the main
 * application class to keep concerns separated.</p>
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {
    // Marker configuration — all cron values live in application.yml
    // and are injected into ComplianceScheduler via @Value.
}
