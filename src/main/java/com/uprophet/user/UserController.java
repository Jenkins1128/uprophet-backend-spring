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
import com.uprophet.user.dto.FavoriterResponse;
import com.uprophet.user.dto.FavoritingResponse;
import com.uprophet.user.dto.NotificationCountResponse;
import com.uprophet.user.dto.NotificationResponse;
import com.uprophet.user.dto.PhotoRequest;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.SearchRequest;
import com.uprophet.user.dto.SearchResultResponse;
import com.uprophet.user.dto.ToUserRequest;
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
    private final NotificationService notificationService;

    public UserController(QuoteService quoteService, UserService userService, NotificationService notificationService) {
        this.quoteService = quoteService;
        this.userService = userService;
        this.notificationService = notificationService;
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

    @PostMapping("/search")
    public List<SearchResultResponse> search(
            @Valid @RequestBody SearchRequest request,
            Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return userService.search(user.username(), request.search());
    }

    @PostMapping("/favorite")
    public ResponseEntity<Void> favorite(@Valid @RequestBody ToUserRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        userService.favorite(user.username(), request.toUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unfavorite")
    public ResponseEntity<Void> unfavorite(@Valid @RequestBody ToUserRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        userService.unfavorite(user.username(), request.toUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/favoriters")
    public List<FavoriterResponse> favoriters(
            @Valid @RequestBody UsernameRequest request,
            Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return userService.favoriters(user.username(), request.username());
    }

    @PostMapping("/favoriting")
    public List<FavoritingResponse> favoriting(
            @Valid @RequestBody UsernameRequest request,
            Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return userService.favoriting(user.username(), request.username());
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> notifications(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return notificationService.notifications(user.username());
    }

    @GetMapping("/getNotificationCount")
    public NotificationCountResponse notificationCount(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return new NotificationCountResponse(notificationService.unreadCount(user.username()));
    }
}
