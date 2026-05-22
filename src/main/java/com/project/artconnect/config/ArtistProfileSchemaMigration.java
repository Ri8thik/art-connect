package com.project.artconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArtistProfileSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE app.artist_profiles ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT FALSE");
            jdbcTemplate.execute("ALTER TABLE app.artist_profiles ADD COLUMN IF NOT EXISTS featured_rank INTEGER");
            jdbcTemplate.execute("ALTER TABLE app.artist_profiles ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED'");
            jdbcTemplate.execute("ALTER TABLE app.artist_profiles ADD COLUMN IF NOT EXISTS approval_reason TEXT");
            jdbcTemplate.execute("UPDATE app.artist_profiles SET featured = FALSE WHERE featured IS NULL");
            jdbcTemplate.execute("UPDATE app.artist_profiles SET approval_status = 'APPROVED' WHERE approval_status IS NULL");
            log.info("Artist profile schema migration completed successfully");
        } catch (Exception e) {
            log.warn("Artist profile schema migration skipped/failed: {}", e.getMessage());
        }
    }
}

