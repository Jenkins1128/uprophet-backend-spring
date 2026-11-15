package com.uprophet.user;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uprophet.auth.security.AuthUser;
import com.uprophet.quote.QuoteService;
import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.user.dto.BioRequest;
import com.uprophet.user.dto.CurrentUserInfoResponse;
import com.uprophet.user.dto.PhotoRequest;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.UserInfoResponse;
import com.uprophet.user.dto.UsernameRequest;

import jakarta.validation.Valid;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api")
public class UserController {

    private static final JsonMapper JSON = JsonMapper.shared();

    private final QuoteService quoteService;
    private final UserService userService;

    public UserController(QuoteService quoteService, UserService userService) {
        this.quoteService = quoteService;
        this.userService = userService;
    }

    @GetMapping("/currentUser")
    public ResponseEntity<String> currentUser(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(JSON.writeValueAsString(user.username()));
    }

    @PostMapping("/profile")
    public List<HomeQuoteResponse> profile(
            @Valid @RequestBody UsernameRequest request,
            Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return quoteService.profile(user.id(), request.username());
    }

    @PostMapping("/userInfo")
    public UserInfoResponse userInfo(
            @Valid @RequestBody UsernameRequest request,
            Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return userService.userInfo(user.username(), request.username());
    }

    @PostMapping("/getPhoto")
    public PhotoResponse photo(@Valid @RequestBody UsernameRequest request) {
        return userService.photo(request.username());
    }

    @GetMapping("/currentUserInfo")
    public CurrentUserInfoResponse currentUserInfo(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return userService.currentUserInfo(user.username());
    }

    @PutMapping("/savebio")
    public ResponseEntity<Void> saveBio(@Valid @RequestBody BioRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        userService.saveBio(user.username(), request.bio());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/uploadPic")
    public ResponseEntity<Void> uploadPic(@Valid @RequestBody PhotoRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        userService.savePhoto(user.username(), request.name(), request.image());
        return ResponseEntity.ok().build();
    }
}
