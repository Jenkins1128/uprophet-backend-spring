package com.uprophet.quote;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteNotificationRepository extends JpaRepository<QuoteNotification, Long> {

    @Query("""
            select new com.uprophet.quote.QuoteNoticeRow(
                    n.id, n.notice, n.quotesId, n.date, q.userName)
            from QuoteNotification n, Quote q
            where q.id = n.quotesId and q.userName = :username
            order by n.id desc
            """)
    List<QuoteNoticeRow> findLatestForOwner(@Param("username") String username, Pageable pageable);

    @Modifying
    @Query("""
            update QuoteNotification n
            set n.readFlag = 1
            where n.readFlag = 0 and n.quotesId in :quoteIds
            """)
    void markReadForQuotes(@Param("quoteIds") Collection<Long> quoteIds);

    long countByQuotesIdInAndReadFlag(Collection<Long> quotesIds, int readFlag);
}
