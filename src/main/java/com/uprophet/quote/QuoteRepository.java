package com.uprophet.quote;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    @Query("select q.userName, max(q.id) from Quote q where q.userName in :names group by q.userName")
    List<Object[]> findLatestIdsByUserNames(@Param("names") Collection<String> names);
}
