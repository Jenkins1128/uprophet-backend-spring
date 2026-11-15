package com.uprophet.quote;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.uprophet.quote.dto.CommentResponse;
import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.user.NotificationService;
import com.uprophet.user.UserRepository;
import com.uprophet.user.UserService;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
@AutoConfigureMockMvc
class QuoteControllerTest {

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

    @Test
    void homeWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void homeReturnsTheFeedForTheSignedInUser() throws Exception {
        when(quoteService.home(42L, "ada")).thenReturn(List.of(
                new HomeQuoteResponse(2L, "ada", "Later", "second", "2024-02-02 03:04:05", 3L, true)));
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(get("/api/").with(signedIn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].userName").value("ada"))
                .andExpect(jsonPath("$[0].title").value("Later"))
                .andExpect(jsonPath("$[0].quote").value("second"))
                .andExpect(jsonPath("$[0].datePosted").value("2024-02-02 03:04:05"))
                .andExpect(jsonPath("$[0].likeCount").value(3))
                .andExpect(jsonPath("$[0].didLike").value(true));

        mockMvc.perform(get("/api").with(signedIn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("ada"));
    }

    @Test
    void exploreWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/explore"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void exploreReturnsTheServiceResult() throws Exception {
        when(quoteService.explore(42L)).thenReturn(List.of(
                new HomeQuoteResponse(9L, "bea", "Title", "words", "2024-01-01 00:00:00", 4L, true)));
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(get("/api/explore").with(signedIn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].userName").value("bea"))
                .andExpect(jsonPath("$[0].likeCount").value(4))
                .andExpect(jsonPath("$[0].didLike").value(true));
    }

    @Test
    void createQuoteWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/createQuote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hello\",\"quote\":\"World\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void createQuoteReturnsTheNewQuote() throws Exception {
        when(quoteService.create("ada", "Hello", "World")).thenReturn(
                new HomeQuoteResponse(11L, "ada", "Hello", "World", "2024-02-02 03:04:05", 0L, false));
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(post("/api/createQuote").with(signedIn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hello\",\"quote\":\"World\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.userName").value("ada"))
                .andExpect(jsonPath("$.title").value("Hello"))
                .andExpect(jsonPath("$.quote").value("World"))
                .andExpect(jsonPath("$.datePosted").value("2024-02-02 03:04:05"))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.didLike").value(false));
    }

    @Test
    void createQuoteRejectsABlankTitle() throws Exception {
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(post("/api/createQuote").with(signedIn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"quote\":\"World\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void likeWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteId\":9}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }

    @Test
    void likeAndUnlikeCallTheService() throws Exception {
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(post("/api/like").with(signedIn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteId\":9}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/unlike").with(signedIn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteId\":9}"))
                .andExpect(status().isOk());

        verify(quoteService).like(42L, "ada", 9L);
        verify(quoteService).unlike(42L, 9L);
    }

    @Test
    void quoteAndCommentsReturnTheServiceResult() throws Exception {
        when(quoteService.quote(42L, 9L)).thenReturn(
                new HomeQuoteResponse(9L, "bea", "Title", "words", "2024-01-01 00:00:00", 2L, true));
        when(quoteService.comments(9L)).thenReturn(List.of(
                new CommentResponse(4L, "nice", "ada", "2024-02-02 00:00:00")));
        when(quoteService.addComment("ada", 9L, "nice")).thenReturn(
                new CommentResponse(4L, "nice", "ada", "2024-02-02 00:00:00"));
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(get("/api/quotes/9").with(signedIn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("bea"))
                .andExpect(jsonPath("$.didLike").value(true));
        mockMvc.perform(get("/api/quotes/9/comments").with(signedIn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("nice"))
                .andExpect(jsonPath("$[0].commenter").value("ada"))
                .andExpect(jsonPath("$[0].datePosted").value("2024-02-02 00:00:00"));
        mockMvc.perform(post("/api/addComment").with(signedIn)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quoteId\":9,\"comment\":\"nice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.comment").value("nice"));
    }
}
