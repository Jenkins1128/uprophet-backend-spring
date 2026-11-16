package com.uprophet.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
public class Like {

    @Id
    @Column(name = "users_id", nullable = false)
    private Long usersId;

    @Id
    @Column(name = "quotes_id", nullable = false)
    private Long quotesId;

    public Long getUsersId() {
        return usersId;
    }

    public void setUsersId(Long usersId) {
        this.usersId = usersId;
    }

    public Long getQuotesId() {
        return quotesId;
    }

    public void setQuotesId(Long quotesId) {
        this.quotesId = quotesId;
    }
}
