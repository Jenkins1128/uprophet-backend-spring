package com.uprophet.auth.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.uprophet.user.User;
import com.uprophet.user.UserRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtCookieFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_POSTS = Set.of(
            "/api/signup",
            "/api/signin",
            "/api/forgotPassword",
            "/api/changePasswordSignIn",
            "/api/changePassword");

    private final JwtService jwtService;
    private final AuthCookie authCookie;
    private final UserRepository userRepository;

    public JwtCookieFilter(JwtService jwtService, AuthCookie authCookie, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authCookie = authCookie;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return HttpMethod.POST.matches(request.getMethod()) && PUBLIC_POSTS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = authCookie.read(request);
        if (accessToken == null || accessToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            authenticate(jwtService.verifyAccess(accessToken));
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            refresh(accessToken, request, response, filterChain);
        }
    }

    private void refresh(String accessToken, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            AuthUser presented = jwtService.readUnverified(accessToken);
            User user = userRepository.findByUserName(presented.username()).orElse(null);
            if (user == null || user.getRefreshToken() == null || user.getRefreshToken().isBlank()) {
                unauthorized(response, "Not authorized, session expired");
                return;
            }
            AuthUser refreshed = jwtService.verifyRefresh(user.getRefreshToken());
            if (refreshed.id() != presented.id() || !refreshed.username().equals(presented.username())) {
                unauthorized(response, "Not authorized, please login again");
                return;
            }
            authCookie.write(response, jwtService.signAccess(refreshed.id(), refreshed.username()));
            authenticate(refreshed);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            unauthorized(response, "Not authorized, please login again");
        }
    }

    private static void authenticate(AuthUser user) {
        var authentication = new UsernamePasswordAuthenticationToken(user, null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
