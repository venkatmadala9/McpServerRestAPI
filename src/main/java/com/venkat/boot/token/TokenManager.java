package com.venkat.boot.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TokenManager {

 private static class CachedToken {
     final String token;
     final Instant expiresAt;
     CachedToken(String token, Instant expiresAt) {
         this.token = token; this.expiresAt = expiresAt;
     }
 }

 private final TokenClient tokenClient;
 private final ObjectMapper mapper = new ObjectMapper();
 private final AtomicReference<CachedToken> cache = new AtomicReference<>();

 private final long skewSeconds;

 public TokenManager(TokenClient tokenClient,
                     @Value("${security.oauth2.skew-seconds:30}") long skewSeconds) {
     this.tokenClient = tokenClient;
     this.skewSeconds = skewSeconds;
 }

 public synchronized String getValidToken() {
     var cur = cache.get();
     if (cur == null || isExpired(cur.expiresAt)) {
         refreshToken();
         cur = cache.get();
     }
     return cur.token;
 }

 public synchronized void refreshToken() {
     TokenResponse tr = tokenClient.fetchToken();
     String accessToken = tr.getAccessToken();
     Instant expiresAt = deriveExpiry(tr, accessToken, skewSeconds);
     cache.set(new CachedToken(accessToken, expiresAt));
 }

 private boolean isExpired(Instant expiresAt) {
     return expiresAt == null || Instant.now().isAfter(expiresAt);
 }

 private Instant deriveExpiry(TokenResponse tr, String jwt, long skew) {
     if (tr.getExpiresIn() > 0) {
         return Instant.now().plusSeconds(Math.max(1, tr.getExpiresIn() - skew));
     }
     // Fallback: parse JWT 'exp' claim
     try {
         String[] parts = jwt.split("\\.");
         if (parts.length < 2) return Instant.now().plusSeconds(60);
         String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
         JsonNode node = mapper.readTree(payloadJson);
         long exp = node.path("exp").asLong(0);
         if (exp > 0) {
             return Instant.ofEpochSecond(exp).minusSeconds(skew);
         }
     } catch (Exception ignore) {}
     return Instant.now().plusSeconds(60); // conservative fallback
 }
}