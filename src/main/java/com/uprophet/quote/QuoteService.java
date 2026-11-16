package com.uprophet.quote;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uprophet.common.error.ApiException;
import com.uprophet.quote.dto.CommentResponse;
import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.user.FavoritingRepository;

@Service
public class QuoteService {

    private static final DateTimeFormatter POSTED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FavoritingRepository favoritingRepository;
    private final QuoteRepository quoteRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final QuoteNotificationRepository quoteNotificationRepository;

    public QuoteService(
            FavoritingRepository favoritingRepository,
            QuoteRepository quoteRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            QuoteNotificationRepository quoteNotificationRepository) {
        this.favoritingRepository = favoritingRepository;
        this.quoteRepository = quoteRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.quoteNotificationRepository = quoteNotificationRepository;
    }

    @Transactional(readOnly = true)
    public List<HomeQuoteResponse> home(long userId, String username) {
        List<String> names = new ArrayList<>(favoritingRepository.findToUsersByFromUser(username));
        names.add(username);
        List<Long> ids = latestIds(quoteRepository.findLatestIdsByUserNames(names));
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Quote> quotes = new ArrayList<>(quoteRepository.findAllById(ids));
        quotes.sort(Comparator.comparing(quote -> quote.getDatePosted(), Comparator.nullsLast(Comparator.reverseOrder())));
        return toResponses(userId, quotes);
    }

    @Transactional(readOnly = true)
    public List<HomeQuoteResponse> profile(long userId, String username) {
        List<Quote> quotes = quoteRepository.findByUserNameOrderByIdDesc(username);
        if (quotes.isEmpty()) {
            return List.of();
        }
        return toResponses(userId, quotes);
    }

    @Transactional(readOnly = true)
    public List<HomeQuoteResponse> explore(long userId) {
        List<Long> ids = new ArrayList<>();
        for (Number id : quoteRepository.findRandomIds()) {
            if (id != null) {
                ids.add(id.longValue());
            }
        }
        if (ids.isEmpty()) {
            return List.of();
        }
        return toResponses(userId, new ArrayList<>(quoteRepository.findAllById(ids)));
    }

    @Transactional
    public HomeQuoteResponse create(String username, String title, String quoteText) {
        Quote quote = new Quote();
        quote.setUserName(username);
        quote.setTitle(title);
        quote.setQuote(quoteText);
        quote.setDatePosted(LocalDateTime.now(ZoneOffset.UTC).withNano(0));
        Quote saved = quoteRepository.save(quote);
        if (saved.getId() == null) {
            throw new IllegalStateException("Failed to retrieve created quote");
        }
        return new HomeQuoteResponse(
                saved.getId(),
                saved.getUserName(),
                saved.getTitle(),
                saved.getQuote(),
                format(saved.getDatePosted()),
                0,
                false);
    }

    @Transactional
    public void like(long userId, String username, long quoteId) {
        LikeId id = new LikeId(userId, quoteId);
        if (likeRepository.existsById(id)) {
            return;
        }
        Like like = new Like();
        like.setUsersId(userId);
        like.setQuotesId(quoteId);
        likeRepository.save(like);
        notify(username + " liked your quote.", quoteId);
    }

    @Transactional
    public void unlike(long userId, long quoteId) {
        likeRepository.findById(new LikeId(userId, quoteId)).ifPresent(like -> likeRepository.delete(like));
    }

    @Transactional(readOnly = true)
    public HomeQuoteResponse quote(long userId, long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ApiException("Quote not found", 404));
        return toResponses(userId, List.of(quote)).get(0);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> comments(long quoteId) {
        return commentRepository.findByQuotesIdOrderByIdDesc(quoteId).stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getComment(),
                        comment.getCommenter(),
                        format(comment.getDatePosted())))
                .toList();
    }

    @Transactional
    public CommentResponse addComment(String username, long quoteId, String text) {
        LocalDateTime postedAt = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        Comment comment = new Comment();
        comment.setQuotesId(quoteId);
        comment.setComment(text);
        comment.setCommenter(username);
        comment.setDatePosted(postedAt);
        Comment saved = commentRepository.save(comment);
        if (saved.getId() == null) {
            throw new IllegalStateException("Failed to retrieve added comment");
        }
        notify(username + " commented on your quote.", quoteId);
        return new CommentResponse(saved.getId(), saved.getComment(), saved.getCommenter(), format(postedAt));
    }

    @Transactional
    public void delete(long quoteId) {
        quoteRepository.deleteById(quoteId);
    }

    private void notify(String notice, long quoteId) {
        QuoteNotification notification = new QuoteNotification();
        notification.setNotice(notice);
        notification.setQuotesId(quoteId);
        notification.setReadFlag(0);
        notification.setDate(LocalDateTime.now(ZoneOffset.UTC).withNano(0));
        quoteNotificationRepository.save(notification);
    }

    private List<HomeQuoteResponse> toResponses(long userId, List<Quote> quotes) {
        List<Long> ids = quotes.stream().map(quote -> quote.getId()).toList();
        Map<Long, Long> likeCounts = likeCounts(ids);
        Set<Long> liked = new HashSet<>(likeRepository.findLikedQuoteIds(userId, ids));
        return quotes.stream()
                .map(quote -> new HomeQuoteResponse(
                        quote.getId(),
                        quote.getUserName(),
                        quote.getTitle(),
                        quote.getQuote(),
                        format(quote.getDatePosted()),
                        likeCounts.getOrDefault(quote.getId(), 0L),
                        liked.contains(quote.getId())))
                .toList();
    }

    private static List<Long> latestIds(List<Object[]> rows) {
        List<Long> ids = new ArrayList<>();
        for (Object[] row : rows) {
            ids.add(((Number) row[1]).longValue());
        }
        return ids;
    }

    private Map<Long, Long> likeCounts(List<Long> ids) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : likeRepository.countByQuoteIds(ids)) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private static String format(LocalDateTime postedAt) {
        if (postedAt == null) {
            return null;
        }
        return POSTED_AT.format(postedAt);
    }
}
