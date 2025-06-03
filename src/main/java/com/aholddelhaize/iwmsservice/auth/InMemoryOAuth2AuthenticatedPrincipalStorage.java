package com.aholddelhaize.iwmsservice.auth;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a lightweight alternative to previously used class
 * org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
 * which has become deprecated starting from Spring 6, with no replacement provided.
 */
@Log4j2
public class InMemoryOAuth2AuthenticatedPrincipalStorage {

    private static final int DEFAULT_FLUSH_INTERVAL = 50;
    private static final String EXPIRATION_ATTRIBUTE = "exp";
    private static final Duration TTL_FOR_TOKENS_WITHOUT_EXPIRATION_ATTRIBUTE = Duration.ofMinutes(30);

    private final ConcurrentHashMap<String, OAuth2AuthenticatedPrincipal> tokenToPrincipalMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TokenExpiration> tokenToExpirationMap = new ConcurrentHashMap<>();
    private final DelayQueue<TokenExpiration> tokenExpirationQueue = new DelayQueue<>();
    private final AtomicInteger flushCounter = new AtomicInteger(0);

    private final int flushInterval;

    public InMemoryOAuth2AuthenticatedPrincipalStorage() {
        this(DEFAULT_FLUSH_INTERVAL);
    }

    public InMemoryOAuth2AuthenticatedPrincipalStorage(int flushInterval) {
        this.flushInterval = flushInterval;
    }

    public int getStoredTokensCount() {
        return tokenToPrincipalMap.size();
    }

    public Optional<OAuth2AuthenticatedPrincipal> loadPrincipal(String token) {
        try {
            return Optional.ofNullable(tokenToPrincipalMap.get(token)).filter(this::isNotExpired);
        } catch (Exception e) {
            log.error("Failed to load OAuth2 authenticated principal from in-memory storage. Exception class: {}",
                    e.getClass().getName());
            return Optional.empty();
        }
    }

    public void savePrincipal(String token, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        try {
            flushExpiredTokensIfNeeded();
            tokenToPrincipalMap.put(token, authenticatedPrincipal);
            saveTokenExpiration(token, authenticatedPrincipal);
        } catch (Exception e) {
            log.error("Failed to save OAuth2 authenticated principal to in-memory storage. Exception class: {}",
                    e.getClass().getName());
        }
    }

    private void saveTokenExpiration(String token, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        TokenExpiration tokenExpiration = new TokenExpiration(token, getExpirationTime(authenticatedPrincipal).toEpochMilli());
        boolean tokenExpirationDequeued = tokenExpirationQueue.remove(tokenToExpirationMap.put(token, tokenExpiration));
        tokenExpirationQueue.put(tokenExpiration);
        if (tokenExpirationDequeued) {
            log.debug("TokenExpiration was updated for some token");
        }
    }

    private Instant getExpirationTime(OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        Object expirationObj = authenticatedPrincipal.getAttribute(EXPIRATION_ATTRIBUTE);
        if (expirationObj instanceof Instant expirationInstant) {
            return expirationInstant;
        }
        return Instant.now().plus(TTL_FOR_TOKENS_WITHOUT_EXPIRATION_ATTRIBUTE);
    }

    private boolean isNotExpired(OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        final Instant expirationTime = getExpirationTime(authenticatedPrincipal);
        return Instant.now().isBefore(expirationTime);
    }

    private void flushExpiredTokensIfNeeded() {
        if (flushCounter.incrementAndGet() > flushInterval) {
            flush();
            flushCounter.set(0);
        }
    }

    private void flush() {
        for (TokenExpiration expiry = tokenExpirationQueue.poll(); expiry != null; expiry = tokenExpirationQueue.poll()) {
            tokenToExpirationMap.remove(expiry.getToken(), expiry);
            tokenToPrincipalMap.remove(expiry.getToken());
        }
    }
}
