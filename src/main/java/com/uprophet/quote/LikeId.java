package com.uprophet.quote;

import java.io.Serializable;
import java.util.Objects;

public class LikeId implements Serializable {

    private Long usersId;

    private Long quotesId;

    public LikeId() {
    }

    public LikeId(Long usersId, Long quotesId) {
        this.usersId = usersId;
        this.quotesId = quotesId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LikeId)) {
            return false;
        }
        LikeId likeId = (LikeId) other;
        return Objects.equals(usersId, likeId.usersId) && Objects.equals(quotesId, likeId.quotesId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersId, quotesId);
    }
}
