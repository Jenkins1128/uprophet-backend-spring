package com.uprophet.quote;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uprophet.auth.LoginRepository;
import com.uprophet.auth.security.AuthUser;
import com.uprophet.quote.dto.HomeQuoteResponse;
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
}
