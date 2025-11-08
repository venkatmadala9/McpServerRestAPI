package com.venkat.boot.config;

import com.venkat.boot.token.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthorizedApiClient {

 private final RestClient apiRestClient;
 private final TokenManager tokenManager;

 public AuthorizedApiClient(RestClient apiRestClient, TokenManager tokenManager) {
     this.apiRestClient = apiRestClient;
     this.tokenManager = tokenManager;
 }

 public String get(String pathTemplate, Object... uriVars) {
     String token = tokenManager.getValidToken();
     try {
         return apiRestClient.get()
                 .uri(pathTemplate, uriVars)
                 .accept(MediaType.APPLICATION_JSON)
                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                 .retrieve()
                 .onStatus(HttpStatusCode::isError, (req, res) -> { /* let exception be thrown */ })
                 .body(String.class);
     } catch (Exception ex) {
         if (is401(ex)) {
             tokenManager.refreshToken();
             String newToken = tokenManager.getValidToken();
             return apiRestClient.get()
                     .uri(pathTemplate, uriVars)
                     .accept(MediaType.APPLICATION_JSON)
                     .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken)
                     .retrieve()
                     .body(String.class);
         }
         throw ex;
     }
 }

 private boolean is401(Exception ex) {
     String msg = ex.getMessage();
     return msg != null && (msg.contains("401") || msg.toLowerCase().contains("unauthorized"));
 }
}