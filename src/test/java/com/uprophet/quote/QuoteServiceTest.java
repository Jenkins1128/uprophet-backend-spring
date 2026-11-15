package com.uprophet.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uprophet.quote.dto.HomeQuoteResponse;
import com.uprophet.user.FavoritingRepository;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private FavoritingRepository favoritingRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private QuoteService quoteService;

    @Test
    void homeKeepsOneQuotePerUserAndSortsByDate() {
        when(favoritingRepository.findToUsersByFromUser("ada")).thenReturn(List.of("bea"));
        when(quoteRepository.findLatestIdsByUserNames(any())).thenReturn(List.<Object[]>of(
                new Object[] {"ada", 2L},
                new Object[] {"bea", 5L},
                new Object[] {"cy", 9L}));
        when(quoteRepository.findAllById(List.of(2L, 5L, 9L))).thenReturn(List.of(
                quote(5L, "bea", "Older", "first", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
                quote(9L, "cy", "None", "blank", null),
                quote(2L, "ada", "Later", "second", LocalDateTime.of(2024, 2, 2, 3, 4, 5))));
        when(likeRepository.countByQuoteIds(List.of(2L, 5L, 9L)))
                .thenReturn(List.<Object[]>of(new Object[] {2L, 3L}));
        when(likeRepository.findLikedQuoteIds(42L, List.of(2L, 5L, 9L))).thenReturn(List.of(2L));

        List<HomeQuoteResponse> home = quoteService.home(42L, "ada");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> names = ArgumentCaptor.forClass(Collection.class);
        verify(quoteRepository).findLatestIdsByUserNames(names.capture());
        assertThat(names.getValue()).containsExactly("bea", "ada");
        assertThat(home).containsExactly(
                new HomeQuoteResponse(2L, "ada", "Later", "second", "2024-02-02 03:04:05", 3L, true),
                new HomeQuoteResponse(5L, "bea", "Older", "first", "2024-01-01 00:00:00", 0L, false),
                new HomeQuoteResponse(9L, "cy", "None", "blank", null, 0L, false));
    }

    @Test
    void homeIsEmptyWhenThereAreNoQuotes() {
        when(favoritingRepository.findToUsersByFromUser("ada")).thenReturn(List.of());
        when(quoteRepository.findLatestIdsByUserNames(List.of("ada"))).thenReturn(List.of());

        assertThat(quoteService.home(42L, "ada")).isEmpty();
        verify(likeRepository, never()).countByQuoteIds(any());
        verify(likeRepository, never()).findLikedQuoteIds(anyLong(), any());
    }

    private static Quote quote(long id, String userName, String title, String text, LocalDateTime postedAt) {
        Quote quote = new Quote();
        quote.setId(id);
        quote.setUserName(userName);
        quote.setTitle(title);
        quote.setQuote(text);
        quote.setDatePosted(postedAt);
        return quote;
    }
}
