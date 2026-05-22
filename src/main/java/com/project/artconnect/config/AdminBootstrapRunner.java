package com.project.artconnect.config;

import com.project.artconnect.common.enums.UserRole;
import com.project.artconnect.modules.users.entity.User;
import com.project.artconnect.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@hunarhub.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";
    private static final String DEFAULT_ADMIN_NAME = "System Admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.info("Default admin already exists: {}", DEFAULT_ADMIN_EMAIL);
            return;
        }

        User admin = User.builder()
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .displayName(DEFAULT_ADMIN_NAME)
                .roles(new HashSet<>(Set.of(UserRole.ADMIN)))
                .build();

        userRepository.save(admin);
        log.info("Created default admin account: {}", DEFAULT_ADMIN_EMAIL);
    }
}

