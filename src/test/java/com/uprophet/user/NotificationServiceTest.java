package com.uprophet.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.uprophet.quote.QuoteNoticeRow;
import com.uprophet.quote.QuoteNotificationRepository;
import com.uprophet.quote.QuoteRepository;
import com.uprophet.user.dto.NotificationResponse;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteNotificationRepository quoteNotificationRepository;

    @Mock
    private FavoriteNotificationRepository favoriteNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notificationsCombineNewestFirstAndMarkThemRead() {
        when(quoteNotificationRepository.findLatestForOwner("ada", PageRequest.of(0, 10))).thenReturn(List.of(
                new QuoteNoticeRow(2L, "bea liked your quote.", 9L, LocalDateTime.of(2024, 1, 1, 0, 0), "ada")));
        when(favoriteNotificationRepository.findTop10ByToUserOrderByIdDesc("ada")).thenReturn(List.of(
                favorite(5L, "cy favorited you.", "ada", LocalDateTime.of(2024, 2, 2, 0, 0)),
                favorite(4L, "dee favorited you.", "ada", null)));
        when(quoteRepository.findIdsByUserName("ada")).thenReturn(List.of(9L));

        assertThat(notificationService.notifications("ada")).containsExactly(
                new NotificationResponse(5L, "cy favorited you.", null, "2024-02-02 00:00:00", null, "ada", "favorite"),
                new NotificationResponse(2L, "bea liked your quote.", 9L, "2024-01-01 00:00:00", "ada", null, "quote"),
                new NotificationResponse(4L, "dee favorited you.", null, null, null, "ada", "favorite"));

        verify(quoteNotificationRepository).markReadForQuotes(List.of(9L));
        verify(favoriteNotificationRepository).markRead("ada");
    }

    @Test
    void notificationsSkipQuoteUpdatesWhenTheUserHasNoQuotes() {
        when(quoteNotificationRepository.findLatestForOwner("ada", PageRequest.of(0, 10))).thenReturn(List.of());
        when(favoriteNotificationRepository.findTop10ByToUserOrderByIdDesc("ada")).thenReturn(List.of());
        when(quoteRepository.findIdsByUserName("ada")).thenReturn(List.of());

        assertThat(notificationService.notifications("ada")).isEmpty();

        verify(quoteNotificationRepository, never()).markReadForQuotes(any());
        verify(favoriteNotificationRepository).markRead("ada");
    }

    @Test
    void unreadCountAddsQuoteAndFavoriteNotices() {
        when(quoteRepository.findIdsByUserName("ada")).thenReturn(List.of(9L));
        when(quoteNotificationRepository.countByQuotesIdInAndReadFlag(List.of(9L), 0)).thenReturn(2L);
        when(favoriteNotificationRepository.countByToUserAndReadFlag("ada", 0)).thenReturn(1L);

        assertThat(notificationService.unreadCount("ada")).isEqualTo(3L);
    }

    @Test
    void unreadCountSkipsQuoteNoticesWhenTheUserHasNoQuotes() {
        when(quoteRepository.findIdsByUserName("ada")).thenReturn(List.of());
        when(favoriteNotificationRepository.countByToUserAndReadFlag("ada", 0)).thenReturn(1L);

        assertThat(notificationService.unreadCount("ada")).isEqualTo(1L);
        verify(quoteNotificationRepository, never()).countByQuotesIdInAndReadFlag(any(), anyInt());
    }

    private static FavoriteNotification favorite(long id, String notice, String toUser, LocalDateTime date) {
        FavoriteNotification notification = new FavoriteNotification();
        notification.setId(id);
        notification.setNotice(notice);
        notification.setToUser(toUser);
        notification.setDate(date);
        return notification;
    }
}
