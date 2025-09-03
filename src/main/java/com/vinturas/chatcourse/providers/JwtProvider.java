package com.vinturas.chatcourse.providers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
public class JwtProvider implements TokenProvider {

    private final KeyProvider keyProvider;

    // Constructor injection
    public JwtProvider(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    @Override
    public Map<String, String> decode(String token) {
        DecodedJWT jwt = JWT.decode(token);
        PublicKey publicKey = keyProvider.getPublicKey(jwt.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
        algorithm.verify(jwt);

        boolean expired = jwt
                .getExpiresAtAsInstant()
                .atZone(ZoneId.systemDefault())
                .isBefore(ZonedDateTime.now());

        if (expired) {
            throw new RuntimeException("Token expired");
        }

        return Map.of(
                "id", jwt.getSubject(),
                "name", jwt.getClaim("name").asString(),
                "email", jwt.getClaim("email").asString()
        );
    }
}

