package com.uprophet.quote;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @Query("select l.quotesId, count(l) from Like l where l.quotesId in :ids group by l.quotesId")
    List<Object[]> countByQuoteIds(@Param("ids") Collection<Long> ids);

    @Query("select l.quotesId from Like l where l.usersId = :userId and l.quotesId in :ids")
    List<Long> findLikedQuoteIds(@Param("userId") long userId, @Param("ids") Collection<Long> ids);
}
