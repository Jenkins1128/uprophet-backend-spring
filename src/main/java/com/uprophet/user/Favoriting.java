package com.uprophet.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "favoriting")
@IdClass(FavoritingId.class)
public class Favoriting {

    @Id
    @Column(name = "from_user", nullable = false, length = 20)
    private String fromUser;

    @Id
    @Column(name = "to_user", nullable = false, length = 20)
    private String toUser;

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }
}
