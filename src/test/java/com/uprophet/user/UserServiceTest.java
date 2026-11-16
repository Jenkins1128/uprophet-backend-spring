package com.uprophet.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.uprophet.user.dto.CurrentUserInfoResponse;
import com.uprophet.user.dto.FavoriterResponse;
import com.uprophet.user.dto.FavoritingResponse;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.SearchResultResponse;
import com.uprophet.user.dto.UserInfoResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FavoritingRepository favoritingRepository;

    @Mock
    private FavoriteNotificationRepository favoriteNotificationRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void userInfoIncludesFollowCountsAndBio() {
        User bea = new User();
        bea.setBio("hello");
        when(userRepository.findByUserName("bea")).thenReturn(Optional.of(bea));
        when(favoritingRepository.existsByFromUserAndToUser("ada", "bea")).thenReturn(true);
        when(favoritingRepository.countByToUser("bea")).thenReturn(2L);
        when(favoritingRepository.countByFromUser("bea")).thenReturn(3L);

        assertThat(userService.userInfo("ada", "bea")).isEqualTo(
                new UserInfoResponse("ada", true, 2L, 3L, "hello"));
    }

    @Test
    void userInfoForAMissingUserHasNoBio() {
        when(userRepository.findByUserName("missing")).thenReturn(Optional.empty());
        when(favoritingRepository.existsByFromUserAndToUser("ada", "missing")).thenReturn(false);
        when(favoritingRepository.countByToUser("missing")).thenReturn(0L);
        when(favoritingRepository.countByFromUser("missing")).thenReturn(0L);

        assertThat(userService.userInfo("ada", "missing")).isEqualTo(
                new UserInfoResponse("ada", false, 0L, 0L, null));
    }

    @Test
    void photoReturnsTheStoredText() {
        User bea = new User();
        bea.setPhoto("abc123".getBytes(StandardCharsets.UTF_8));
        when(userRepository.findByUserName("bea")).thenReturn(Optional.of(bea));

        assertThat(userService.photo("bea")).isEqualTo(new PhotoResponse("abc123"));
    }

    @Test
    void photoIsNullWhenTheUserOrPhotoIsMissing() {
        User bea = new User();
        when(userRepository.findByUserName("bea")).thenReturn(Optional.of(bea));
        when(userRepository.findByUserName("missing")).thenReturn(Optional.empty());

        assertThat(userService.photo("bea")).isEqualTo(new PhotoResponse(null));
        assertThat(userService.photo("missing")).isEqualTo(new PhotoResponse(null));
    }

    @Test
    void currentUserInfoReturnsTheSignedInBio() {
        User ada = new User();
        ada.setBio("hello");
        when(userRepository.findByUserName("ada")).thenReturn(Optional.of(ada));

        assertThat(userService.currentUserInfo("ada")).isEqualTo(new CurrentUserInfoResponse("ada", "hello"));
    }

    @Test
    void saveBioUpdatesTheSignedInUser() {
        User ada = new User();
        when(userRepository.findByUserName("ada")).thenReturn(Optional.of(ada));

        userService.saveBio("ada", "hello");

        assertThat(ada.getBio()).isEqualTo("hello");
        verify(userRepository).save(ada);
    }

    @Test
    void saveBioDoesNothingWhenTheUserIsMissing() {
        when(userRepository.findByUserName("ada")).thenReturn(Optional.empty());

        userService.saveBio("ada", "hello");

        verify(userRepository, never()).save(any());
    }

    @Test
    void savePhotoStoresTheImageText() {
        User ada = new User();
        when(userRepository.findByUserName("ada")).thenReturn(Optional.of(ada));

        userService.savePhoto("ada", "pic.png", "abc123");

        assertThat(ada.getPhotoName()).isEqualTo("pic.png");
        assertThat(new String(ada.getPhoto(), StandardCharsets.UTF_8)).isEqualTo("abc123");
        verify(userRepository).save(ada);
    }

    @Test
    void searchMarksFollowedUsers() {
        when(userRepository.searchByUserName(org.mockito.ArgumentMatchers.eq("ad"), any(Pageable.class)))
                .thenReturn(List.of(user(1L, "ada"), user(3L, "cyd")));
        when(favoritingRepository.findToUsersByFromUserAndToUserIn("bea", List.of("ada", "cyd")))
                .thenReturn(List.of("ada"));

        assertThat(userService.search("bea", "ad")).containsExactly(
                new SearchResultResponse(1L, "ada", "bea", true),
                new SearchResultResponse(3L, "cyd", "bea", false));
    }

    @Test
    void searchSkipsFollowsWhenNobodyMatches() {
        when(userRepository.searchByUserName(org.mockito.ArgumentMatchers.eq("zzz"), any(Pageable.class)))
                .thenReturn(List.of());

        assertThat(userService.search("bea", "zzz")).isEmpty();
        verify(favoritingRepository, never()).findToUsersByFromUserAndToUserIn(any(), any());
    }

    @Test
    void favoriteStoresTheFollowAndANotification() {
        when(favoritingRepository.existsByFromUserAndToUser("ada", "bea")).thenReturn(false);
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC).withNano(0);

        userService.favorite("ada", "bea");

        ArgumentCaptor<Favoriting> follow = ArgumentCaptor.forClass(Favoriting.class);
        verify(favoritingRepository).save(follow.capture());
        assertThat(follow.getValue().getFromUser()).isEqualTo("ada");
        assertThat(follow.getValue().getToUser()).isEqualTo("bea");
        ArgumentCaptor<FavoriteNotification> notice = ArgumentCaptor.forClass(FavoriteNotification.class);
        verify(favoriteNotificationRepository).save(notice.capture());
        assertThat(notice.getValue().getNotice()).isEqualTo("ada favorited you.");
        assertThat(notice.getValue().getToUser()).isEqualTo("bea");
        assertThat(notice.getValue().getReadFlag()).isZero();
        assertThat(notice.getValue().getDate()).isBetween(before, LocalDateTime.now(ZoneOffset.UTC).withNano(0).plusSeconds(1));
    }

    @Test
    void favoriteDoesNothingWhenAlreadyFollowing() {
        when(favoritingRepository.existsByFromUserAndToUser("ada", "bea")).thenReturn(true);

        userService.favorite("ada", "bea");

        verify(favoritingRepository, never()).save(any());
        verify(favoriteNotificationRepository, never()).save(any());
    }

    @Test
    void unfavoriteRemovesTheFollow() {
        Favoriting follow = new Favoriting();
        follow.setFromUser("ada");
        follow.setToUser("bea");
        when(favoritingRepository.findById(new FavoritingId("ada", "bea"))).thenReturn(Optional.of(follow));

        userService.unfavorite("ada", "bea");

        verify(favoritingRepository).delete(follow);
    }

    @Test
    void unfavoriteDoesNothingWhenTheFollowIsMissing() {
        when(favoritingRepository.findById(new FavoritingId("ada", "bea"))).thenReturn(Optional.empty());

        userService.unfavorite("ada", "bea");

        verify(favoritingRepository, never()).delete(any());
    }

    @Test
    void favoritersMarkWhoTheSignedInUserAlreadyFollows() {
        when(favoritingRepository.findFromUsersByToUser("bea")).thenReturn(List.of("ada", "cy"));
        when(favoritingRepository.findToUsersByFromUserAndToUserIn("ada", List.of("ada", "cy")))
                .thenReturn(List.of("cy"));

        assertThat(userService.favoriters("ada", "bea")).containsExactly(
                new FavoriterResponse("ada", "ada", false),
                new FavoriterResponse("cy", "ada", true));
    }

    @Test
    void favoritersSkipTheFollowLookupWhenNobodyFavoritedThem() {
        when(favoritingRepository.findFromUsersByToUser("bea")).thenReturn(List.of());

        assertThat(userService.favoriters("ada", "bea")).isEmpty();
        verify(favoritingRepository, never()).findToUsersByFromUserAndToUserIn(any(), any());
    }

    @Test
    void favoritingMarkWhoTheSignedInUserAlreadyFollows() {
        when(favoritingRepository.findToUsersByFromUser("bea")).thenReturn(List.of("ada", "cy"));
        when(favoritingRepository.findToUsersByFromUserAndToUserIn("ada", List.of("ada", "cy")))
                .thenReturn(List.of("ada"));

        assertThat(userService.favoriting("ada", "bea")).containsExactly(
                new FavoritingResponse("ada", "ada", true),
                new FavoritingResponse("cy", "ada", false));
    }

    private static User user(long id, String userName) {
        User user = new User();
        user.setId(id);
        user.setUserName(userName);
        return user;
    }
}
