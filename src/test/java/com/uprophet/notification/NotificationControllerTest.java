package com.uprophet.notification;

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
import com.uprophet.notification.dto.NotificationResponse;
import com.uprophet.quote.QuoteService;
import com.uprophet.user.UserRepository;
import com.uprophet.user.UserService;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private QuoteService quoteService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LoginRepository loginRepository;

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

    private static org.springframework.test.web.servlet.request.RequestPostProcessor signedIn() {
        return authentication(new UsernamePasswordAuthenticationToken(new AuthUser(42L, "ada"), null, List.of()));
    }
}
