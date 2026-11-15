package com.uprophet.user;

import java.io.Serializable;
import java.util.Objects;

public class FavoritingId implements Serializable {

    private String fromUser;

    private String toUser;

    public FavoritingId() {
    }

    public FavoritingId(String fromUser, String toUser) {
        this.fromUser = fromUser;
        this.toUser = toUser;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FavoritingId)) {
            return false;
        }
        FavoritingId favoritingId = (FavoritingId) other;
        return Objects.equals(fromUser, favoritingId.fromUser) && Objects.equals(toUser, favoritingId.toUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromUser, toUser);
    }
}
