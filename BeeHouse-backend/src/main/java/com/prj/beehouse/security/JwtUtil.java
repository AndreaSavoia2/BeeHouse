package com.prj.beehouse.security;


import com.prj.beehouse.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility component responsible for generating, parsing, and validating
 * JSON Web Tokens (JWT).
 * <p>
 * Tokens are signed using an HMAC secret key and contain the user's email
 * as the subject. The expiration time is configured through application properties.
 */
@Component
@Slf4j
public class JwtUtil {

    /**
     * Secret key used to sign and verify JWT tokens.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Token expiration time expressed in milliseconds.
     */
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Cryptographic key used to digitally sign and verify JWT tokens.
     */
    private SecretKey key;

    /**
     * Initializes the cryptographic key from the configured secret.
     * <p>
     * This method is automatically invoked after dependency injection
     * has completed.
     */
    @PostConstruct
    public void init() {
        // Converts the secret string into a byte array and generates a secure SecretKey
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a digitally signed JWT token for the specified user.
     * <p>
     * The token contains:
     * <ul>
     *     <li>The user's email as the subject.</li>
     *     <li>The user's role, name, and id as custom claims.</li>
     *     <li>The issue timestamp.</li>
     *     <li>The expiration timestamp.</li>
     * </ul>
     *
     * @param user the user for whom the token is generated
     * @return the generated JWT token
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", user.getAuthority().getAuthorityName());
        customClaims.put("name", user.getName());
        customClaims.put("lastname", user.getLastname());
        customClaims.put("userId", user.getId());

        return Jwts.builder()
                .claims(customClaims)
                .subject(user.getUsername())           // Sets the user's identity (Subject)
                .issuedAt(now)                      // Sets the issue timestamp
                .expiration(expiryDate)             // Sets the expiration timestamp
                .signWith(key)                      // Digitally signs the payload with the secret key
                .compact();
    }

    /**
     * Extracts the user identifier stored in the JWT subject claim.
     *
     * @param token the JWT token to parse
     * @return the email address stored in the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid,
     *                                      expired, or has been tampered with
     */
    public String getUserFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)                    // Configures the key to verify the signature
                .build()
                .parseSignedClaims(token)           // Parses and cryptographically validates the token
                .getPayload()                       // Retrieves the claims body
                .getSubject();                      // Extracts the "sub" (Subject) field
    }

    /**
     * Validates a JWT token.
     * <p>
     * This method verifies:
     * <ul>
     *     <li>The integrity of the token signature.</li>
     *     <li>The token expiration date.</li>
     *     <li>The overall token format.</li>
     * </ul>
     *
     * @param token the JWT token to validate
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateJwtToken(String token) {
        try {
            // If parsing succeeds, the signature is authentic and the token is valid
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Catches any anomaly (expiration, wrong signature, invalid format)
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }
}
