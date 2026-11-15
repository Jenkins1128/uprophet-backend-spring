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

import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.user.FavoritingRepository;

@Service
public class QuoteService {

    private static final DateTimeFormatter POSTED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FavoritingRepository favoritingRepository;
    private final QuoteRepository quoteRepository;
    private final LikeRepository likeRepository;

    public QuoteService(
            FavoritingRepository favoritingRepository,
            QuoteRepository quoteRepository,
            LikeRepository likeRepository) {
        this.favoritingRepository = favoritingRepository;
        this.quoteRepository = quoteRepository;
        this.likeRepository = likeRepository;
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
