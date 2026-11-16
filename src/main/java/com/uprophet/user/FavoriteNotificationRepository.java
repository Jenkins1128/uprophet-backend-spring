package com.uprophet.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteNotificationRepository extends JpaRepository<FavoriteNotification, Long> {

    List<FavoriteNotification> findTop10ByToUserOrderByIdDesc(String toUser);

    @Modifying
    @Query("update FavoriteNotification n set n.readFlag = 1 where n.toUser = :toUser")
    void markRead(@Param("toUser") String toUser);

    long countByToUserAndReadFlag(String toUser, int readFlag);
}
