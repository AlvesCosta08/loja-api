package com.alvescosta.loja.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn
) {
    public static TokenResponse bearer(String token, long expiresInMs) {
        return new TokenResponse(token, "Bearer", expiresInMs / 1000);
    }
}
