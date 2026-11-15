package com.uprophet.quote;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    @Query("select q.userName, max(q.id) from Quote q where q.userName in :names group by q.userName")
    List<Object[]> findLatestIdsByUserNames(@Param("names") Collection<String> names);

    List<Quote> findByUserNameOrderByIdDesc(String userName);

    @Query("select q.id from Quote q where q.userName = :userName")
    List<Long> findIdsByUserName(@Param("userName") String userName);

    @Query(value = "select id from quotes order by rand() limit 20", nativeQuery = true)
    List<Number> findRandomIds();
}
