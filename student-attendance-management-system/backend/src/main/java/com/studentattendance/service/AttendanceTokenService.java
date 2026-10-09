package com.studentattendance.service;

import java.security.SecureRandom;
import java.util.Base64;

import com.studentattendance.repository.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class AttendanceTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SessionRepository sessionRepository;

    public AttendanceTokenService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public String generateUniqueToken() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            byte[] bytes = new byte[TOKEN_BYTES];
            secureRandom.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            if (sessionRepository.findByAttendanceToken(token).isEmpty()) {
                return token;
            }
        }
        throw new IllegalStateException("Unable to generate a unique attendance token.");
    }
}
