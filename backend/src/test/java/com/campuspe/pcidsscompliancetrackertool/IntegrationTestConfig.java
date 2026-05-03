package com.campuspe.pcidsscompliancetrackertool;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

/**
 * Test-only Spring configuration that provides mock beans required to
 * start the full application context in the {@code integration-test} profile
 * without external services (SMTP).
 *
 * <p>This config is auto-detected by {@link @SpringBootTest} because it lives
 * in the same package as the test classes. The {@code @Primary} annotation
 * ensures that if any other {@link JavaMailSender} bean exists (e.g. from
 * {@code spring-boot-starter-mail}), the mock takes precedence.</p>
 */
@TestConfiguration
@Profile("integration-test")
public class IntegrationTestConfig {

    /**
     * Replaces the real {@link JavaMailSender} with a Mockito mock so that
     * integration tests never attempt a live SMTP connection.
     *
     * @return a no-op mock instance
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
