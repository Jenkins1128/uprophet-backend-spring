package com.uprophet.quote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uprophet.common.error.ApiException;
import com.uprophet.quote.dto.CommentResponse;
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

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private QuoteNotificationRepository quoteNotificationRepository;

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

    @Test
    void profileKeepsRepositoryOrderAndLikeState() {
        when(quoteRepository.findByUserNameOrderByIdDesc("bea")).thenReturn(List.of(
                quote(9L, "bea", "Newer id", "second", LocalDateTime.of(2024, 1, 1, 0, 0, 0)),
                quote(2L, "bea", "Older id", "first", LocalDateTime.of(2024, 6, 1, 0, 0, 0))));
        when(likeRepository.countByQuoteIds(List.of(9L, 2L)))
                .thenReturn(List.<Object[]>of(new Object[] {2L, 4L}));
        when(likeRepository.findLikedQuoteIds(42L, List.of(9L, 2L))).thenReturn(List.of(9L));

        assertThat(quoteService.profile(42L, "bea")).containsExactly(
                new HomeQuoteResponse(9L, "bea", "Newer id", "second", "2024-01-01 00:00:00", 0L, true),
                new HomeQuoteResponse(2L, "bea", "Older id", "first", "2024-06-01 00:00:00", 4L, false));
    }

    @Test
    void profileIsEmptyWhenTheUserHasNoQuotes() {
        when(quoteRepository.findByUserNameOrderByIdDesc("bea")).thenReturn(List.of());

        assertThat(quoteService.profile(42L, "bea")).isEmpty();
        verify(likeRepository, never()).countByQuoteIds(any());
        verify(likeRepository, never()).findLikedQuoteIds(anyLong(), any());
    }

    @Test
    void exploreKeepsTheLoadedOrderAndLikeState() {
        when(quoteRepository.findRandomIds()).thenReturn(List.of(2L, 9L));
        when(quoteRepository.findAllById(List.of(2L, 9L))).thenReturn(List.of(
                quote(2L, "ada", "Older id", "first", LocalDateTime.of(2024, 6, 1, 0, 0, 0)),
                quote(9L, "bea", "Newer id", "second", LocalDateTime.of(2024, 1, 1, 0, 0, 0))));
        when(likeRepository.countByQuoteIds(List.of(2L, 9L)))
                .thenReturn(List.<Object[]>of(new Object[] {9L, 4L}));
        when(likeRepository.findLikedQuoteIds(42L, List.of(2L, 9L))).thenReturn(List.of(2L));

        assertThat(quoteService.explore(42L)).containsExactly(
                new HomeQuoteResponse(2L, "ada", "Older id", "first", "2024-06-01 00:00:00", 0L, true),
                new HomeQuoteResponse(9L, "bea", "Newer id", "second", "2024-01-01 00:00:00", 4L, false));
    }

    @Test
    void exploreIsEmptyWhenThereAreNoQuotes() {
        when(quoteRepository.findRandomIds()).thenReturn(List.of());

        assertThat(quoteService.explore(42L)).isEmpty();
        verify(likeRepository, never()).countByQuoteIds(any());
        verify(likeRepository, never()).findLikedQuoteIds(anyLong(), any());
    }

    @Test
    void createStoresTheSignedInUsersQuote() {
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> {
            Quote quote = invocation.getArgument(0);
            quote.setId(11L);
            return quote;
        });

        HomeQuoteResponse created = quoteService.create("ada", "Hello", "World");

        ArgumentCaptor<Quote> saved = ArgumentCaptor.forClass(Quote.class);
        verify(quoteRepository).save(saved.capture());
        assertThat(saved.getValue().getUserName()).isEqualTo("ada");
        assertThat(saved.getValue().getTitle()).isEqualTo("Hello");
        assertThat(saved.getValue().getQuote()).isEqualTo("World");
        assertThat(saved.getValue().getDatePosted())
                .isBetween(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2), LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        assertThat(created).isEqualTo(new HomeQuoteResponse(
                11L,
                "ada",
                "Hello",
                "World",
                saved.getValue().getDatePosted().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                0L,
                false));
    }

    @Test
    void likeSavesTheLikeAndANotification() {
        when(likeRepository.existsById(new LikeId(42L, 9L))).thenReturn(false);

        quoteService.like(42L, "ada", 9L);

        ArgumentCaptor<Like> like = ArgumentCaptor.forClass(Like.class);
        verify(likeRepository).save(like.capture());
        assertThat(like.getValue().getUsersId()).isEqualTo(42L);
        assertThat(like.getValue().getQuotesId()).isEqualTo(9L);
        ArgumentCaptor<QuoteNotification> notice = ArgumentCaptor.forClass(QuoteNotification.class);
        verify(quoteNotificationRepository).save(notice.capture());
        assertThat(notice.getValue()).extracting("notice", "quotesId", "readFlag")
                .containsExactly("ada liked your quote.", 9L, 0);
    }

    @Test
    void unlikeRemovesTheLike() {
        Like like = new Like();
        like.setUsersId(42L);
        like.setQuotesId(9L);
        when(likeRepository.findById(new LikeId(42L, 9L))).thenReturn(Optional.of(like));

        quoteService.unlike(42L, 9L);

        verify(likeRepository).delete(like);
    }

    @Test
    void quoteReturnsLikeState() {
        when(quoteRepository.findById(9L)).thenReturn(Optional.of(
                quote(9L, "bea", "Title", "words", LocalDateTime.of(2024, 1, 1, 0, 0, 0))));
        when(likeRepository.countByQuoteIds(List.of(9L))).thenReturn(List.<Object[]>of(new Object[] {9L, 2L}));
        when(likeRepository.findLikedQuoteIds(42L, List.of(9L))).thenReturn(List.of(9L));

        assertThat(quoteService.quote(42L, 9L)).isEqualTo(
                new HomeQuoteResponse(9L, "bea", "Title", "words", "2024-01-01 00:00:00", 2L, true));
    }

    @Test
    void quoteIsNotFoundWhenTheIdIsMissing() {
        when(quoteRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.quote(42L, 9L))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).status())
                .isEqualTo(404);
    }

    @Test
    void commentsKeepNewestIdFirst() {
        when(commentRepository.findByQuotesIdOrderByIdDesc(9L)).thenReturn(List.of(
                comment(4L, "second", "ada", LocalDateTime.of(2024, 2, 2, 0, 0, 0)),
                comment(3L, "first", "bea", LocalDateTime.of(2024, 1, 1, 0, 0, 0))));

        assertThat(quoteService.comments(9L)).containsExactly(
                new CommentResponse(4L, "second", "ada", "2024-02-02 00:00:00"),
                new CommentResponse(3L, "first", "bea", "2024-01-01 00:00:00"));
    }

    @Test
    void addCommentStoresTheCommentAndANotification() {
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(8L);
            return comment;
        });

        CommentResponse created = quoteService.addComment("ada", 9L, "nice");

        assertThat(created.id()).isEqualTo(8L);
        assertThat(created.comment()).isEqualTo("nice");
        assertThat(created.commenter()).isEqualTo("ada");
        ArgumentCaptor<QuoteNotification> notice = ArgumentCaptor.forClass(QuoteNotification.class);
        verify(quoteNotificationRepository).save(notice.capture());
        assertThat(notice.getValue()).extracting("notice", "quotesId").containsExactly("ada commented on your quote.", 9L);
    }

    private static Comment comment(long id, String text, String commenter, LocalDateTime postedAt) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setComment(text);
        comment.setCommenter(commenter);
        comment.setDatePosted(postedAt);
        return comment;
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
