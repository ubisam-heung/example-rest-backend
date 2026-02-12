package com.example.rest.auth;

import com.example.rest.auth.dto.AuthRequest;
import com.example.rest.auth.dto.AuthResponse;
import com.example.rest.auth.dto.AuthTokens;
import com.example.rest.auth.dto.RegisterRequest;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    AuthTokens tokens = authService.register(request);
    return buildAuthResponse(tokens);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
    AuthTokens tokens = authService.login(request);
    return buildAuthResponse(tokens);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
    @CookieValue(value = "refresh_token", required = false) String refreshToken
  ) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다");
    }

    AuthTokens tokens = authService.refresh(refreshToken);
    return buildAuthResponse(tokens);
  }

  @SuppressWarnings("null")
  private ResponseEntity<AuthResponse> buildAuthResponse(AuthTokens tokens) {
    ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
      .httpOnly(true)
      .secure(false)
      .path("/api/auth/refresh")
      .sameSite("Lax")
      .maxAge(Duration.ofMillis(refreshExpirationMs))
      .build();

    AuthResponse response = new AuthResponse(tokens.accessToken(), tokens.username());
    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body(response);
  }
}
