package com.carplatform.user.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("customDb")
public class CustomDbHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public CustomDbHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (value != null && value == 1) {
                return Health.up().withDetail("sanityQuery", "ok").build();
            }
            return Health.down().withDetail("sanityQuery", "unexpected-result").build();
        } catch (Exception exception) {
            return Health.down(exception).withDetail("sanityQuery", "failed").build();
        }
    }
}
