package com.studentattendance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DevelopmentDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataSeeder.class);

    @Override
    public void run(String... args) {
        log.info("Development seed hook is enabled. No seed data is defined for Phase 2.");
    }
}
