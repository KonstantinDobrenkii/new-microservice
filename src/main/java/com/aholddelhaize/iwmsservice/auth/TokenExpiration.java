package com.aholddelhaize.iwmsservice.auth;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class TokenExpiration implements Delayed {

    @Getter
    private final String token;
    private final long expirationDateInMillis;

    public TokenExpiration(String token, long expirationDateInMillis) {
        this.token = token;
        this.expirationDateInMillis = expirationDateInMillis;
    }

    @Override
    public int compareTo(Delayed other) {
        if (this == other) {
            return 0;
        } else {
            long diff = this.getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(diff, 0L);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenExpiration that = (TokenExpiration) o;
        return getToken().equals(that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken());
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return this.expirationDateInMillis - System.currentTimeMillis();
    }

}
