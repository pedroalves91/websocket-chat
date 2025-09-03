package com.vinturas.chatcourse.providers;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;

@Component
public class JwtKeyProvider implements  KeyProvider {
    private final UrlJwkProvider provider;

    public JwtKeyProvider(@Value("${app.auth.jwks-url}") final String jwksUrl) {
        try {
            this.provider = new UrlJwkProvider(new URL(jwksUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable("public-key")
    @Override
    public PublicKey getPublicKey(String keyId) {
        try {
            final Jwk jwt = provider.get(keyId);
            return jwt.getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(e);
        }
    }
}
