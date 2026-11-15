package com.uprophet.user;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uprophet.user.dto.CurrentUserInfoResponse;
import com.uprophet.user.dto.PhotoResponse;
import com.uprophet.user.dto.UserInfoResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FavoritingRepository favoritingRepository;

    public UserService(UserRepository userRepository, FavoritingRepository favoritingRepository) {
        this.userRepository = userRepository;
        this.favoritingRepository = favoritingRepository;
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

    private static String text(byte[] photo) {
        if (photo == null || photo.length == 0) {
            return null;
        }
        return new String(photo, StandardCharsets.UTF_8);
    }
}
