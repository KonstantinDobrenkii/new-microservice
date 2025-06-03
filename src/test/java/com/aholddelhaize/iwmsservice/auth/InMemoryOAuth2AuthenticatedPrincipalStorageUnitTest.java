package com.aholddelhaize.iwmsservice.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class InMemoryOAuth2AuthenticatedPrincipalStorageUnitTest {

    private static final String EXP_ATTRIBUTE = "exp";
    private static final Duration FIVE_DAYS = Duration.ofDays(5);
    private static final Instant FUTURE_DATE = Instant.now().plus(FIVE_DAYS);
    private static final Instant PAST_DATE = Instant.now().minus(FIVE_DAYS);

    private static final String TEST_TOKEN = "testToken";
    private static final int FLUSH_INTERVAL = 5;

    private InMemoryOAuth2AuthenticatedPrincipalStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryOAuth2AuthenticatedPrincipalStorage(FLUSH_INTERVAL);
    }

    @Test
    void shouldSaveAuthenticatedPrincipal_whenTokenNotExpired() {
        OAuth2AuthenticatedPrincipal testPrincipal = buildTestAuthenticatedPrincipal(FUTURE_DATE);

        storage.savePrincipal(TEST_TOKEN, testPrincipal);

        assertEquals(1, storage.getStoredTokensCount());
    }

    @Test
    void shouldLoadAuthenticatedPrincipal_whenTokenNotExpired() {
        OAuth2AuthenticatedPrincipal testPrincipal = buildTestAuthenticatedPrincipal(FUTURE_DATE);
        storage.savePrincipal(TEST_TOKEN, testPrincipal);

        Optional<OAuth2AuthenticatedPrincipal> optPrincipal = storage.loadPrincipal(TEST_TOKEN);

        assertTrue(optPrincipal.isPresent());
        assertEquals(testPrincipal, optPrincipal.get());
    }

    @Test
    void shouldSaveAuthenticatedPrincipal_whenTokenExpired() {
        OAuth2AuthenticatedPrincipal testPrincipal = buildTestAuthenticatedPrincipal(PAST_DATE);

        storage.savePrincipal(TEST_TOKEN, testPrincipal);

        assertEquals(1, storage.getStoredTokensCount());
    }

    @Test
    void shouldNotLoadAuthenticatedPrincipal_whenTokenExpired() {
        OAuth2AuthenticatedPrincipal testPrincipal = buildTestAuthenticatedPrincipal(PAST_DATE);
        storage.savePrincipal(TEST_TOKEN, testPrincipal);

        Optional<OAuth2AuthenticatedPrincipal> optPrincipal = storage.loadPrincipal(TEST_TOKEN);

        assertTrue(optPrincipal.isEmpty());
    }

    @Test
    void shouldSaveAllPrincipalsWithExpiredTokens_whenFlushCounterIsNotExceeded() {
        int amountToSave = FLUSH_INTERVAL - 1;
        OAuth2AuthenticatedPrincipal principalPrototype = buildTestAuthenticatedPrincipal(PAST_DATE);

        for (int i = 0; i < amountToSave; i++) {
            storage.savePrincipal("Principal" + i, principalPrototype);
        }

        assertEquals(amountToSave, storage.getStoredTokensCount());
    }

    @Test
    void shouldSaveAllPrincipalsWithNonExpiredTokens_whenFlushCounterIsNotExceeded() {
        int amountToSave = FLUSH_INTERVAL - 1;
        OAuth2AuthenticatedPrincipal principalPrototype = buildTestAuthenticatedPrincipal(FUTURE_DATE);

        for (int i = 0; i < amountToSave; i++) {
            storage.savePrincipal("Principal" + i, principalPrototype);
        }

        assertEquals(amountToSave, storage.getStoredTokensCount());
    }

    @Test
    void shouldFlushPrincipalsWithExpiredTokens_whenFlushCounterIsExceeded() {
        int toBeSavedAfterFlush = 3;
        int amountToSave = FLUSH_INTERVAL + toBeSavedAfterFlush;
        OAuth2AuthenticatedPrincipal principalPrototype = buildTestAuthenticatedPrincipal(PAST_DATE);

        for (int i = 0; i < amountToSave; i++) {
            storage.savePrincipal("Principal" + i, principalPrototype);
        }

        assertEquals(toBeSavedAfterFlush, storage.getStoredTokensCount());
    }

    @Test
    void shouldNotFlushPrincipalsWithNonExpiredTokens_whenFlushCounterIsExceeded() {
        int toBeSavedAfterFlush = 3;
        int amountToSave = FLUSH_INTERVAL + toBeSavedAfterFlush;
        OAuth2AuthenticatedPrincipal principalPrototype = buildTestAuthenticatedPrincipal(FUTURE_DATE);

        for (int i = 0; i < amountToSave; i++) {
            storage.savePrincipal("Principal" + i, principalPrototype);
        }

        assertEquals(amountToSave, storage.getStoredTokensCount());
    }

    private OAuth2AuthenticatedPrincipal buildTestAuthenticatedPrincipal(Instant expirationDate) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(EXP_ATTRIBUTE, expirationDate);
        return new DefaultOAuth2AuthenticatedPrincipal(attributes, AuthorityUtils.NO_AUTHORITIES);
    }
}
