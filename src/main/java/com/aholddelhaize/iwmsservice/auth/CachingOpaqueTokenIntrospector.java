package com.aholddelhaize.iwmsservice.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.List;

import static com.aholddelhaize.iwmsservice.constants.IwmsServiceConstants.SCOPE_AUTHORITY_PREFIX;

public class CachingOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final InMemoryOAuth2AuthenticatedPrincipalStorage inMemoryAuthenticatedPrincipalStorage;
    private final OpaqueTokenIntrospector delegate;

    public CachingOpaqueTokenIntrospector(InMemoryOAuth2AuthenticatedPrincipalStorage storage,
                                          OpaqueTokenIntrospector delegate) {
        this.inMemoryAuthenticatedPrincipalStorage = storage;
        this.delegate = delegate;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        return inMemoryAuthenticatedPrincipalStorage.loadPrincipal(token).orElseGet(() -> {
            final OAuth2AuthenticatedPrincipal authenticatedPrincipal = authenticateRemotely(token);
            inMemoryAuthenticatedPrincipalStorage.savePrincipal(token, authenticatedPrincipal);
            return authenticatedPrincipal;
        });
    }

    private OAuth2AuthenticatedPrincipal authenticateRemotely(String token) {
        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);
        return new DefaultOAuth2AuthenticatedPrincipal(principal.getName(), principal.getAttributes(),
                extractAuthorities(principal));
    }

    private List<GrantedAuthority> extractAuthorities(OAuth2AuthenticatedPrincipal principal) {
        List<String> scopes = principal.getAttribute(OAuth2TokenIntrospectionClaimNames.SCOPE);
        if (scopes == null) {
            return AuthorityUtils.NO_AUTHORITIES;
        }
        return scopes.stream()
                .map(scope -> SCOPE_AUTHORITY_PREFIX + scope)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
