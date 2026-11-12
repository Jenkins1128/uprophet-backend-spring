package com.uprophet.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uprophet.auth.dto.ForgotPasswordRequest;
import com.uprophet.auth.dto.SigninRequest;
import com.uprophet.auth.dto.SignupRequest;
import com.uprophet.auth.security.AuthCookie;
import com.uprophet.auth.security.AuthUser;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final AuthCookie authCookie;

    public AuthController(AuthService authService, AuthCookie authCookie) {
        this.authService = authService;
        this.authCookie = authCookie;
    }

    @PostMapping("/signup")
    public long signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/signin")
    public ResponseEntity<Void> signin(@Valid @RequestBody SigninRequest request, HttpServletResponse response) {
        authCookie.write(response, authService.signin(request));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        authService.logout(user.username());
        authCookie.clear(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/changePasswordSignIn")
    public ResponseEntity<Void> changePasswordSignIn(@Valid @RequestBody SigninRequest request) {
        authService.verifyCredentials(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody SigninRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
