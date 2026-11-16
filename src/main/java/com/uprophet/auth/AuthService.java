package com.uprophet.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.uprophet.auth.dto.ForgotPasswordRequest;
import com.uprophet.auth.dto.SigninRequest;
import com.uprophet.auth.dto.SignupRequest;
import com.uprophet.auth.security.JwtService;
import com.uprophet.auth.security.PasswordHasher;
import com.uprophet.common.error.ApiException;
import com.uprophet.config.AppProperties;
import com.uprophet.user.User;
import com.uprophet.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final AppProperties properties;

    public AuthService(
            UserRepository userRepository,
            LoginRepository loginRepository,
            PasswordHasher passwordHasher,
            JwtService jwtService,
            AppProperties properties) {
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @Transactional
    public long signup(SignupRequest request) {
        if (userRepository.findByUserName(request.username()).isPresent()
                || loginRepository.findByUserName(request.username()).isPresent()) {
            throw new ApiException("Username already exists", 409);
        }
        User user = new User();
        user.setName(request.name());
        user.setUserName(request.username());
        user.setEmail(request.email());
        user = userRepository.save(user);

        long registeredAt = System.currentTimeMillis();
        Login login = new Login();
        login.setPassword(passwordHasher.hash(request.username(), request.password(), registeredAt));
        login.setUserName(request.username());
        login.setUsersId(user.getId());
        login.setUserRegistered(registeredAt);
        return loginRepository.save(login).getId();
    }

    @Transactional
    public String signin(SigninRequest request) {
        Login login = loginRepository.findByUserName(request.username())
                .filter(row -> passwordHasher.matches(
                        request.username(),
                        request.password(),
                        row.getPassword(),
                        row.getUserRegistered()))
                .orElseThrow(() -> new ApiException("Invalid credentials", 401));
        User user = userRepository.findByUserName(login.getUserName())
                .orElseThrow(() -> new ApiException("Invalid credentials", 401));
        String accessToken = jwtService.signAccess(login.getUsersId(), login.getUserName());
        user.setRefreshToken(jwtService.signRefresh(login.getUsersId(), login.getUserName()));
        userRepository.save(user);
        return accessToken;
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUserName(username).ifPresent(user -> {
            user.setRefreshToken("");
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public void verifyCredentials(SigninRequest request) {
        boolean valid = loginRepository.findByUserName(request.username())
                .filter(row -> passwordHasher.matches(
                        request.username(),
                        request.password(),
                        row.getPassword(),
                        row.getUserRegistered()))
                .isPresent();
        if (!valid) {
            throw new ApiException("Invalid credentials", 401);
        }
    }

    @Transactional
    public void changePassword(SigninRequest request) {
        loginRepository.findByUserName(request.username()).ifPresent(login -> {
            long registeredAt = System.currentTimeMillis();
            login.setPassword(passwordHasher.hash(request.username(), request.password(), registeredAt));
            login.setUserRegistered(registeredAt);
            loginRepository.save(login);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUserName(request.username())
                .orElseThrow(() -> new ApiException("User not found", 404));
        if (user.getEmail() == null || !user.getEmail().equals(request.email())) {
            throw new ApiException("Email mismatch", 400);
        }
        String temporaryPassword = passwordHasher.temporaryPassword();
        long registeredAt = System.currentTimeMillis();
        loginRepository.findByUserName(request.username()).ifPresent(login -> {
            login.setPassword(passwordHasher.hash(request.username(), temporaryPassword, registeredAt));
            login.setUserRegistered(registeredAt);
            loginRepository.save(login);
        });
        sendTemporaryPassword(request.username(), request.email(), temporaryPassword);
    }

    private void sendTemporaryPassword(String username, String email, String temporaryPassword) {
        CreateEmailOptions message = new CreateEmailOptions.Builder()
                .from("Uprophet <noreply@recovery.uprophet.com>")
                .to(email)
                .subject("Uprophet Temporary Password")
                .html("Hello " + username
                        + ",<br><br>Here is your temporary password: <strong>"
                        + temporaryPassword
                        + "</strong> <br><br> <a href=\"https://uprophet.com/changepassword\">Change Password</a>")
                .build();
        try {
            new Resend(properties.resend().apiKey()).emails().send(message);
        } catch (ResendException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }
}
