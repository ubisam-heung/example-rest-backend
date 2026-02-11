package com.example.rest.auth.dto;

public record AuthTokens(String accessToken, String refreshToken, String username) {}
