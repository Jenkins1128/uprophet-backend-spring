package com.uprophet.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritingRepository extends JpaRepository<Favoriting, FavoritingId> {

    @Query("select f.toUser from Favoriting f where f.fromUser = :username")
    List<String> findToUsersByFromUser(@Param("username") String username);
}
