package com.uprophet.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uprophet.common.error.ApiException;
import com.uprophet.quote.LikeRepository;
import com.uprophet.quote.QuoteRepository;
import com.uprophet.user.FavoritingRepository;
import com.uprophet.user.UserRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

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
    void signinSetsTheAccessCookie() throws Exception {
        when(authService.signin(any())).thenReturn("access-token");

        mockMvc.perform(post("/api/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ada\",\"password\":\"secret1\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("upUserId=access-token"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("Secure"),
                        org.hamcrest.Matchers.containsString("SameSite=None"))));
    }

    @Test
    void signinRejectsInvalidCredentials() throws Exception {
        when(authService.signin(any())).thenThrow(new ApiException("Invalid credentials", 401));

        mockMvc.perform(post("/api/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ada\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void signupRejectsAShortUsername() throws Exception {
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"name\":\"Ada\",\"email\":\"ada@uprophet.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void logoutWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized, no token"));
    }
}
