package com.chat.app.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Service
public class ClerkJwtService {

    @Value("${clerk.publishable-key:}")
    private String clerkPublishableKey;

    private JwkProvider jwkProvider;

    @PostConstruct
    public void initialize() throws MalformedURLException {
        if (clerkPublishableKey == null || clerkPublishableKey.isEmpty()) {
            throw new IllegalArgumentException("Clerk publishable key is not configured");
        }

        // Clerk publishable keys are formatted as: pk_<env>_<base64(instanceId)>
        // The base64 portion decodes to the FAPI domain, e.g. "oriented-bass-39.clerk.accounts.dev$"
        String jwksUrl = buildJwksUrl(clerkPublishableKey);
        this.jwkProvider = new JwkProviderBuilder(new URL(jwksUrl))
                .build();
    }

    public String verifyToken(String token) throws JWTVerificationException, Exception {
        if (jwkProvider == null) {
            initialize();
        }

        DecodedJWT jwt = JWT.decode(token);
        Jwk jwk = jwkProvider.get(jwt.getKeyId());
        RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

        Algorithm algorithm = Algorithm.RSA256(publicKey, null);
        JWT.require(algorithm)
                .build()
                .verify(token);

        // Return the subject (the Clerk user ID)
        return jwt.getSubject();
    }

    public DecodedJWT decodeToken(String token) {
        return JWT.decode(token);
    }

    /**
     * Builds the JWKS URL from the Clerk publishable key.
     *
     * Clerk publishable keys have the format: pk_<env>_<base64url-encoded-domain>
     * The base64 segment decodes to the FAPI domain with a trailing '$', e.g.:
     *   pk_test_b3JpZW50ZWQtYmFzcy0zOS5jbGVyay5hY2NvdW50cy5kZXYk
     *   -> base64 part: b3JpZW50ZWQtYmFzcy0zOS5jbGVyay5hY2NvdW50cy5kZXYk
     *   -> decoded:     oriented-bass-39.clerk.accounts.dev$
     *   -> JWKS URL:    https://oriented-bass-39.clerk.accounts.dev/.well-known/jwks.json
     */
    private String buildJwksUrl(String publishableKey) {
        // Split on '_' with a limit of 3 to get [pk, <env>, <base64>]
        String[] parts = publishableKey.split("_", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid Clerk publishable key format: " + publishableKey);
        }

        String base64Part = parts[2];

        // Base64 decode (standard decoder handles both padded and unpadded)
        byte[] decoded = Base64.getDecoder().decode(base64Part);
        String domain = new String(decoded, StandardCharsets.UTF_8);

        // Strip trailing '$' that Clerk appends
        if (domain.endsWith("$")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        return "https://" + domain + "/.well-known/jwks.json";
    }
}
