package com.uprophet.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.uprophet.auth.AuthService;
import com.uprophet.auth.LoginRepository;
import com.uprophet.auth.security.AuthUser;
import com.uprophet.quote.LikeRepository;
import com.uprophet.quote.QuoteRepository;
import com.uprophet.quote.QuoteService;

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
    private AuthService authService;

    @MockitoBean
    private QuoteService quoteService;

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
    void currentUserReturnsTheSignedInUsername() throws Exception {
        var signedIn = authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(42L, "ada"), null, List.of()));

        mockMvc.perform(get("/api/currentUser").with(signedIn))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("ada"));
    }
}
