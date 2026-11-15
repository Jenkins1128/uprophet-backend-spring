package com.uprophet.user;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uprophet.auth.LoginRepository;
import com.uprophet.auth.security.AuthUser;
import com.uprophet.quote.LikeRepository;
import com.uprophet.quote.QuoteRepository;
import com.uprophet.quote.QuoteService;
import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.user.dto.CurrentUserInfoResponse;
import com.uprophet.user.dto.FavoriterResponse;
import com.uprophet.user.dto.FavoritingResponse;
import com.uprophet.user.dto.NotificationResponse;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.SearchResultResponse;
import com.uprophet.user.dto.UserInfoResponse;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuoteService quoteService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LoginRepository loginRepository;

    @MockitoBean
    private QuoteRepository quoteRepository;

    @MockitoBean
    private LikeRepository likeRepository;

    @MockitoBean
    private FavoritingRepository favoritingRepository;

    @Test
    void currentUserWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/currentUser"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void currentUserReturnsTheUsernameAsJson() throws Exception {
        mockMvc.perform(get("/api/currentUser").with(signedIn()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("\"ada\""));
    }

    @Test
    void profileReturnsThatUsersQuotes() throws Exception {
        when(quoteService.profile(42L, "bea")).thenReturn(List.of(
                new HomeQuoteResponse(9L, "bea", "Title", "words", "2024-01-01 00:00:00", 1L, false)));

        mockMvc.perform(post("/api/profile").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bea\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].userName").value("bea"))
                .andExpect(jsonPath("$[0].likeCount").value(1))
                .andExpect(jsonPath("$[0].didLike").value(false));
    }

    @Test
    void userInfoReturnsFollowCountsAndBio() throws Exception {
        when(userService.userInfo("ada", "bea")).thenReturn(new UserInfoResponse("ada", true, 2L, 3L, "hello"));

        mockMvc.perform(post("/api/userInfo").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bea\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUser").value("ada"))
                .andExpect(jsonPath("$.didFavorite").value(true))
                .andExpect(jsonPath("$.favoriters").value(2))
                .andExpect(jsonPath("$.favoriting").value(3))
                .andExpect(jsonPath("$.bio").value("hello"));
    }

    @Test
    void photoReturnsTheStoredText() throws Exception {
        when(userService.photo("bea")).thenReturn(new PhotoResponse("abc123"));

        mockMvc.perform(post("/api/getPhoto").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bea\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photo").value("abc123"));
    }

    @Test
    void currentUserInfoReturnsTheBio() throws Exception {
        when(userService.currentUserInfo("ada")).thenReturn(new CurrentUserInfoResponse("ada", "hello"));

        mockMvc.perform(get("/api/currentUserInfo").with(signedIn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUser").value("ada"))
                .andExpect(jsonPath("$.bio").value("hello"));
    }

    @Test
    void saveBioWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/savebio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"hello\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void saveBioUpdatesTheSignedInUser() throws Exception {
        mockMvc.perform(put("/api/savebio").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"hello\"}"))
                .andExpect(status().isOk());

        verify(userService).saveBio("ada", "hello");
    }

    @Test
    void saveBioRejectsALongBio() throws Exception {
        mockMvc.perform(put("/api/savebio").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"" + "a".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void uploadPicStoresTheImage() throws Exception {
        mockMvc.perform(put("/api/uploadPic").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"pic.png\",\"image\":\"abc123\"}"))
                .andExpect(status().isOk());

        verify(userService).savePhoto("ada", "pic.png", "abc123");
    }

    @Test
    void searchWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"search\":\"ada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void searchReturnsUserNameAndFollowStatus() throws Exception {
        when(userService.search("ada", "be")).thenReturn(List.of(
                new SearchResultResponse(3L, "bea", "ada", true)));

        mockMvc.perform(post("/api/search").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"search\":\"be\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].user_name").value("bea"))
                .andExpect(jsonPath("$[0].currentUser").value("ada"))
                .andExpect(jsonPath("$[0].didFavorite").value(true));
    }

    @Test
    void notificationsWithoutACookieAreUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void notificationsReturnQuoteAndFavoriteNotices() throws Exception {
        when(notificationService.notifications("ada")).thenReturn(List.of(
                new NotificationResponse(5L, "cy favorited you.", null, "2024-02-02 00:00:00", null, "ada", "favorite"),
                new NotificationResponse(2L, "bea liked your quote.", 9L, "2024-01-01 00:00:00", "ada", null, "quote")));

        mockMvc.perform(get("/api/notifications").with(signedIn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].notice").value("cy favorited you."))
                .andExpect(jsonPath("$[0].quotesId").doesNotExist())
                .andExpect(jsonPath("$[0].date").value("2024-02-02 00:00:00"))
                .andExpect(jsonPath("$[0].type").value("favorite"))
                .andExpect(jsonPath("$[1].quotesId").value(9))
                .andExpect(jsonPath("$[1].notice").value("bea liked your quote."))
                .andExpect(jsonPath("$[1].type").value("quote"));
    }

    @Test
    void notificationCountReturnsTheUnreadTotal() throws Exception {
        when(notificationService.unreadCount("ada")).thenReturn(3L);

        mockMvc.perform(get("/api/getNotificationCount").with(signedIn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationCount").value(3));
    }

    @Test
    void favoriteWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"bea\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void favoriteAndUnfavoriteCallTheService() throws Exception {
        mockMvc.perform(post("/api/favorite").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"bea\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/unfavorite").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"bea\"}"))
                .andExpect(status().isOk());

        verify(userService).favorite("ada", "bea");
        verify(userService).unfavorite("ada", "bea");
    }

    @Test
    void favoriteRejectsABlankUsername() throws Exception {
        mockMvc.perform(post("/api/favorite").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toUser\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void favoritersAndFavoritingReturnTheOtherUser() throws Exception {
        when(userService.favoriters("ada", "bea")).thenReturn(List.of(
                new FavoriterResponse("cy", "ada", true)));
        when(userService.favoriting("ada", "bea")).thenReturn(List.of(
                new FavoritingResponse("dee", "ada", false)));

        mockMvc.perform(post("/api/favoriters").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bea\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].from_user").value("cy"))
                .andExpect(jsonPath("$[0].currentUser").value("ada"))
                .andExpect(jsonPath("$[0].didFavorite").value(true));
        mockMvc.perform(post("/api/favoriting").with(signedIn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bea\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].to_user").value("dee"))
                .andExpect(jsonPath("$[0].currentUser").value("ada"))
                .andExpect(jsonPath("$[0].didFavorite").value(false));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor signedIn() {
        return authentication(new UsernamePasswordAuthenticationToken(new AuthUser(42L, "ada"), null, List.of()));
    }
}
