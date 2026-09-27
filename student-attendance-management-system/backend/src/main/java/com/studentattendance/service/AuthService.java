package com.studentattendance.service;

import com.studentattendance.dto.auth.CurrentUserResponse;
import com.studentattendance.dto.auth.LoginRequest;
import com.studentattendance.dto.auth.LoginResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getUser());

        return new LoginResponse(
                token,
                principal.getUser().getId(),
                principal.getUser().getName(),
                principal.getUser().getEmail(),
                principal.getUser().getRole());
    }

    public CurrentUserResponse currentUser(AuthenticatedUser authenticatedUser) {
        return CurrentUserResponse.from(authenticatedUser.getUser());
    }
}
