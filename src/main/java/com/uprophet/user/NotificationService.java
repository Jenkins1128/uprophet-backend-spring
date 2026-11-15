package com.uprophet.user;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uprophet.quote.QuoteNoticeRow;
import com.uprophet.quote.QuoteNotificationRepository;
import com.uprophet.quote.QuoteRepository;
import com.uprophet.user.dto.NotificationResponse;

@Service
public class NotificationService {

    private static final DateTimeFormatter POSTED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final QuoteRepository quoteRepository;
    private final QuoteNotificationRepository quoteNotificationRepository;
    private final FavoriteNotificationRepository favoriteNotificationRepository;

    public NotificationService(
            QuoteRepository quoteRepository,
            QuoteNotificationRepository quoteNotificationRepository,
            FavoriteNotificationRepository favoriteNotificationRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteNotificationRepository = quoteNotificationRepository;
        this.favoriteNotificationRepository = favoriteNotificationRepository;
    }

    @Transactional
    public List<NotificationResponse> notifications(String username) {
        List<QuoteNoticeRow> quoteNotices = quoteNotificationRepository.findLatestForOwner(
                username, PageRequest.of(0, 10));
        List<FavoriteNotification> favoriteNotices = favoriteNotificationRepository
                .findTop10ByToUserOrderByIdDesc(username);

        List<Long> quoteIds = quoteRepository.findIdsByUserName(username);
        if (!quoteIds.isEmpty()) {
            quoteNotificationRepository.markReadForQuotes(quoteIds);
        }
        favoriteNotificationRepository.markRead(username);

        List<NotificationResponse> combined = new ArrayList<>();
        for (QuoteNoticeRow notice : quoteNotices) {
            combined.add(new NotificationResponse(
                    notice.id(),
                    notice.notice(),
                    notice.quotesId(),
                    format(notice.date()),
                    notice.userName(),
                    null,
                    "quote"));
        }
        for (FavoriteNotification notice : favoriteNotices) {
            combined.add(new NotificationResponse(
                    notice.getId(),
                    notice.getNotice(),
                    null,
                    format(notice.getDate()),
                    null,
                    notice.getToUser(),
                    "favorite"));
        }
        combined.sort(Comparator.comparing(
                notification -> notification.date(),
                Comparator.nullsLast(Comparator.reverseOrder())));
        return combined;
    }

    @Transactional(readOnly = true)
    public long unreadCount(String username) {
        List<Long> quoteIds = quoteRepository.findIdsByUserName(username);
        long quoteCount = quoteIds.isEmpty()
                ? 0
                : quoteNotificationRepository.countByQuotesIdInAndReadFlag(quoteIds, 0);
        return quoteCount + favoriteNotificationRepository.countByToUserAndReadFlag(username, 0);
    }

    private static String format(LocalDateTime postedAt) {
        if (postedAt == null) {
            return null;
        }
        return POSTED_AT.format(postedAt);
    }
}
