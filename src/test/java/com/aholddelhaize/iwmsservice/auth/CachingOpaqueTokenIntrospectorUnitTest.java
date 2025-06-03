package com.aholddelhaize.iwmsservice.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.aholddelhaize.iwmsservice.constants.IwmsServiceConstants.SCOPE_AUTHORITY_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingOpaqueTokenIntrospectorUnitTest {

    private static final String TEST_TOKEN = "testToken";
    private static final String TEST_PRINCIPAL_NAME = "TestName";
    private static final List<String> TEST_SCOPES = Arrays.asList("scope1", "scope2");
    private static final List<String> EXPECTED_AUTHORITIES = TEST_SCOPES.stream()
            .map(str -> SCOPE_AUTHORITY_PREFIX + str)
            .toList();

    @Mock
    private InMemoryOAuth2AuthenticatedPrincipalStorage mockStorage;
    @Mock
    private OpaqueTokenIntrospector mockIntrospectorDelegate;
    @InjectMocks
    private CachingOpaqueTokenIntrospector tokenIntrospector;

    @Mock
    private OAuth2AuthenticatedPrincipal mockPrincipal;

    @Test
    void shouldDelegateAuthenticationAndSaveTokenInStorage_whenTokenNotFoundInStorage() {
        when(mockIntrospectorDelegate.introspect(TEST_TOKEN)).thenReturn(mockPrincipal);
        when(mockStorage.loadPrincipal(TEST_TOKEN)).thenReturn(Optional.empty());
        when(mockPrincipal.getName()).thenReturn(TEST_PRINCIPAL_NAME);
        when(mockPrincipal.getAttributes()).thenReturn(Map.of(OAuth2TokenIntrospectionClaimNames.SCOPE, TEST_SCOPES));

        OAuth2AuthenticatedPrincipal authenticatedPrincipal = tokenIntrospector.introspect(TEST_TOKEN);

        assertEquals(TEST_PRINCIPAL_NAME, authenticatedPrincipal.getName());
        verify(mockIntrospectorDelegate).introspect(TEST_TOKEN);
        verify(mockStorage).savePrincipal(TEST_TOKEN, authenticatedPrincipal);
    }

    @Test
    void shouldNotDelegateAuthenticationOrSaveTokenInStorage_whenTokenFoundInStorage() {
        when(mockStorage.loadPrincipal(TEST_TOKEN)).thenReturn(Optional.of(mockPrincipal));

        OAuth2AuthenticatedPrincipal authenticatedPrincipal = tokenIntrospector.introspect(TEST_TOKEN);

        assertEquals(mockPrincipal, authenticatedPrincipal);
        verify(mockIntrospectorDelegate, never()).introspect(anyString());
        verify(mockStorage, never()).savePrincipal(anyString(), any(OAuth2AuthenticatedPrincipal.class));
    }

    @Test
    void shouldPopulateAuthorities_whenDelegatedAuthentication_andScopeAttributeProvided() {
        when(mockIntrospectorDelegate.introspect(TEST_TOKEN)).thenReturn(mockPrincipal);
        when(mockStorage.loadPrincipal(TEST_TOKEN)).thenReturn(Optional.empty());
        when(mockPrincipal.getName()).thenReturn(TEST_PRINCIPAL_NAME);
        when(mockPrincipal.getAttributes()).thenReturn(Map.of(OAuth2TokenIntrospectionClaimNames.SCOPE, TEST_SCOPES));
        when(mockPrincipal.getAttribute(OAuth2TokenIntrospectionClaimNames.SCOPE)).thenReturn(TEST_SCOPES);

        List<String> authorities = tokenIntrospector.introspect(TEST_TOKEN)
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).containsExactlyElementsOf(EXPECTED_AUTHORITIES);
    }

    @Test
    void shouldPopulateEmptyAuthoritiesList_whenDelegatedAuthentication_andScopeAttributeNotProvided() {
        when(mockIntrospectorDelegate.introspect(TEST_TOKEN)).thenReturn(mockPrincipal);
        when(mockStorage.loadPrincipal(TEST_TOKEN)).thenReturn(Optional.empty());
        when(mockPrincipal.getName()).thenReturn(TEST_PRINCIPAL_NAME);
        when(mockPrincipal.getAttributes()).thenReturn(Map.of(OAuth2TokenIntrospectionClaimNames.SCOPE, TEST_SCOPES));

        OAuth2AuthenticatedPrincipal authenticatedPrincipal = tokenIntrospector.introspect(TEST_TOKEN);

        assertEquals(0, authenticatedPrincipal.getAuthorities().size());
    }
}
