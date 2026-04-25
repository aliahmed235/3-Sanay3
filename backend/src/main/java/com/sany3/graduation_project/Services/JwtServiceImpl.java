package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.entites.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret:mySecretKeyForSanay3ApplicationThatIsLongEnoughForHS256}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry:900000}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    /**
     * Token blacklist - stores tokens that have been logged out
     * Uses ConcurrentHashMap for thread-safe operations
     * In production, use Redis for distributed systems
     */
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    @Override
    public String generateAccessToken(User user) {
        log.debug("Generating access token for user: {}", user.getId());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiry);

        String roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().toString())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        log.debug("Generating refresh token for user: {}", user.getId());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiry);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // FIRST: Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted (logged out)");
                return false;
            }

            // THEN: Do normal validation
            Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("email", String.class);
    }

    @Override
    public String getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String roles = claims.get("roles", String.class);
        return roles != null ? roles : "";
    }

    @Override
    public void blacklistToken(String token) {
        log.info("Blacklisting token");
        tokenBlacklist.add(token);

        // Optional: Log size for monitoring
        if (tokenBlacklist.size() % 100 == 0) {
            log.info("Blacklist size: {}", tokenBlacklist.size());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}