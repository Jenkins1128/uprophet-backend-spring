package com.uprophet.quote;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quote_notifications")
public class QuoteNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TINYTEXT")
    private String notice;

    @Column(name = "quotes_id", nullable = false)
    private Long quotesId;

    @Column(name = "`read`", nullable = false)
    private int readFlag;

    @Column
    private LocalDateTime date;

    public Long getId() {
        return id;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public Long getQuotesId() {
        return quotesId;
    }

    public void setQuotesId(Long quotesId) {
        this.quotesId = quotesId;
    }

    public void setReadFlag(int readFlag) {
        this.readFlag = readFlag;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
