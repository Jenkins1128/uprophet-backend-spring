package com.uprophet.user;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uprophet.user.dto.CurrentUserInfoResponse;
import com.uprophet.user.dto.FavoriterResponse;
import com.uprophet.user.dto.FavoritingResponse;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.SearchResultResponse;
import com.uprophet.user.dto.UserInfoResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FavoritingRepository favoritingRepository;
    private final FavoriteNotificationRepository favoriteNotificationRepository;

    public UserService(
            UserRepository userRepository,
            FavoritingRepository favoritingRepository,
            FavoriteNotificationRepository favoriteNotificationRepository) {
        this.userRepository = userRepository;
        this.favoritingRepository = favoritingRepository;
        this.favoriteNotificationRepository = favoriteNotificationRepository;
    }

    @Transactional(readOnly = true)
    public UserInfoResponse userInfo(String currentUsername, String username) {
        String bio = userRepository.findByUserName(username)
                .map(user -> user.getBio())
                .orElse(null);
        return new UserInfoResponse(
                currentUsername,
                favoritingRepository.existsByFromUserAndToUser(currentUsername, username),
                favoritingRepository.countByToUser(username),
                favoritingRepository.countByFromUser(username),
                bio);
    }

    @Transactional(readOnly = true)
    public CurrentUserInfoResponse currentUserInfo(String username) {
        String bio = userRepository.findByUserName(username)
                .map(user -> user.getBio())
                .orElse(null);
        return new CurrentUserInfoResponse(username, bio);
    }

    @Transactional
    public void saveBio(String username, String bio) {
        userRepository.findByUserName(username).ifPresent(user -> {
            user.setBio(bio);
            userRepository.save(user);
        });
    }

    @Transactional
    public void savePhoto(String username, String name, String image) {
        userRepository.findByUserName(username).ifPresent(user -> {
            user.setPhotoName(name);
            user.setPhoto(image.getBytes(StandardCharsets.UTF_8));
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public PhotoResponse photo(String username) {
        String photo = userRepository.findByUserName(username)
                .map(user -> text(user.getPhoto()))
                .orElse(null);
        return new PhotoResponse(photo);
    }

    @Transactional(readOnly = true)
    public List<SearchResultResponse> search(String currentUsername, String search) {
        List<User> users = userRepository.searchByUserName(search, PageRequest.of(0, 15));
        if (users.isEmpty()) {
            return List.of();
        }
        List<String> names = users.stream()
                .map(user -> user.getUserName())
                .filter(name -> name != null && !name.isBlank())
                .toList();
        Set<String> followed = names.isEmpty()
                ? Set.of()
                : new HashSet<>(favoritingRepository.findToUsersByFromUserAndToUserIn(currentUsername, names));
        return users.stream()
                .map(user -> new SearchResultResponse(
                        user.getId(),
                        user.getUserName(),
                        currentUsername,
                        followed.contains(user.getUserName())))
                .toList();
    }

    @Transactional
    public void favorite(String fromUser, String toUser) {
        if (favoritingRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            return;
        }
        Favoriting favoriting = new Favoriting();
        favoriting.setFromUser(fromUser);
        favoriting.setToUser(toUser);
        favoritingRepository.save(favoriting);

        FavoriteNotification notification = new FavoriteNotification();
        notification.setNotice(fromUser + " favorited you.");
        notification.setToUser(toUser);
        notification.setReadFlag(0);
        notification.setDate(LocalDateTime.now(ZoneOffset.UTC).withNano(0));
        favoriteNotificationRepository.save(notification);
    }

    @Transactional
    public void unfavorite(String fromUser, String toUser) {
        favoritingRepository.findById(new FavoritingId(fromUser, toUser))
                .ifPresent(favoriting -> favoritingRepository.delete(favoriting));
    }

    @Transactional(readOnly = true)
    public List<FavoriterResponse> favoriters(String currentUsername, String username) {
        List<String> names = favoritingRepository.findFromUsersByToUser(username).stream()
                .filter(name -> name != null && !name.isBlank())
                .toList();
        if (names.isEmpty()) {
            return List.of();
        }
        Set<String> followed = new HashSet<>(
                favoritingRepository.findToUsersByFromUserAndToUserIn(currentUsername, names));
        return names.stream()
                .map(name -> new FavoriterResponse(name, currentUsername, followed.contains(name)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoritingResponse> favoriting(String currentUsername, String username) {
        List<String> names = favoritingRepository.findToUsersByFromUser(username).stream()
                .filter(name -> name != null && !name.isBlank())
                .toList();
        if (names.isEmpty()) {
            return List.of();
        }
        Set<String> followed = new HashSet<>(
                favoritingRepository.findToUsersByFromUserAndToUserIn(currentUsername, names));
        return names.stream()
                .map(name -> new FavoritingResponse(name, currentUsername, followed.contains(name)))
                .toList();
    }

    private static String text(byte[] photo) {
        if (photo == null || photo.length == 0) {
            return null;
        }
        return new String(photo, StandardCharsets.UTF_8);
    }
}
