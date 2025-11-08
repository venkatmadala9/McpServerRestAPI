package com.venkat.boot.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TokenClient {

 private final RestClient tokenRestClient;
 private final String tokenUriBase;
 private final String tokenUriPath;
 private final String clientId;
 private final String clientSecret;
 private final String grantType;
 private final String username;
 private final String password;
 private final String scope;
 private final boolean basicAuthForClient;

 public TokenClient(
         RestClient tokenRestClient,
         @Value("${security.oauth2.token-uri}") String tokenUri,
         @Value("${security.oauth2.client-id}") String clientId,
         @Value("${security.oauth2.client-secret}") String clientSecret,
         @Value("${security.oauth2.grant-type}") String grantType,
         @Value("${security.oauth2.username:}") String username,
         @Value("${security.oauth2.password:}") String password,
         @Value("${security.oauth2.scope:}") String scope,
         @Value("${security.oauth2.basic-auth-for-client:true}") boolean basicAuthForClient) {

     this.tokenRestClient = tokenRestClient;
     this.tokenUriBase = extractBase(tokenUri);
     this.tokenUriPath = extractPath(tokenUri);
     this.clientId = clientId;
     this.clientSecret = clientSecret;
     this.grantType = grantType;
     this.username = username;
     this.password = password;
     this.scope = scope;
     this.basicAuthForClient = basicAuthForClient;
 }

 public TokenResponse fetchToken() {
     MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
     form.add("grant_type", grantType);

     if ("password".equalsIgnoreCase(grantType)) {
         form.add("username", username);
         form.add("password", password);
     }

     if (!scope.isBlank()) {
         form.add("scope", scope);
     }

     // Client credentials in header or form
     RestClient.RequestBodySpec req = tokenRestClient.post()
             .uri(tokenUriPath)
             .contentType(MediaType.APPLICATION_FORM_URLENCODED);

     if (basicAuthForClient) {
         req = req.header(HttpHeaders.AUTHORIZATION, basicAuthHeader(clientId, clientSecret));
     } else {
         form.add("client_id", clientId);
         form.add("client_secret", clientSecret);
     }

     return req.body(form)
             .retrieve()
             .body(TokenResponse.class);
 }

 private static String basicAuthHeader(String clientId, String clientSecret) {
     String creds = clientId + ":" + clientSecret;
     String base64 = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
     return "Basic " + base64;
 }

 private static String extractBase(String fullUrl) {
     int i = fullUrl.indexOf('/', fullUrl.indexOf("://") + 3);
     return (i > 0) ? fullUrl.substring(0, i) : fullUrl;
 }
 private static String extractPath(String fullUrl) {
     int i = fullUrl.indexOf('/', fullUrl.indexOf("://") + 3);
     return (i > 0) ? fullUrl.substring(i) : "/";
 }
}