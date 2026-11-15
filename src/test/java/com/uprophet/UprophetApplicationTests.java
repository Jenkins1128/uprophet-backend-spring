package com.uprophet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.uprophet.auth.LoginRepository;
import com.uprophet.quote.LikeRepository;
import com.uprophet.quote.QuoteRepository;
import com.uprophet.user.FavoritingRepository;
import com.uprophet.user.UserRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration")
class UprophetApplicationTests {

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
    void contextLoads() {
    }
}
