package com.uprophet.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uprophet.user.dto.CurrentUserInfoResponse;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.UserInfoResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FavoritingRepository favoritingRepository;

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
}
