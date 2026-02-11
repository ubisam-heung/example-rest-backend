package com.example.rest.auth;

import com.example.rest.auth.dto.AuthRequest;
import com.example.rest.auth.dto.AuthTokens;
import com.example.rest.auth.dto.RegisterRequest;
import com.example.rest.user.User;
import com.example.rest.user.UserRepository;
import java.util.regex.Pattern;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
  );
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthTokens register(RegisterRequest request) {
    if (request.email() == null || request.email().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일을 입력해 주세요");
    }
    if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다");
    }
    if (request.username() == null || request.username().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디를 입력해 주세요");
    }
    if (request.password() == null || request.password().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호를 입력해 주세요");
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가입된 아이디입니다.");
    }

    User user = new User(
      request.username(),
      request.email(),
      passwordEncoder.encode(request.password()),
      "ROLE_USER"
    );
    Long nextId = userRepository.findNextAvailableId();
    if (nextId != null) {
      user.setId(nextId);
    }
    userRepository.save(user);

    String accessToken = jwtService.generateAccessToken(user.getUsername());
    String refreshToken = jwtService.generateRefreshToken(user.getUsername());
    return new AuthTokens(accessToken, refreshToken, user.getUsername());
  }

  public AuthTokens login(AuthRequest request) {
    if (request.username() == null || request.username().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디를 입력해 주세요");
    }
    if (request.password() == null || request.password().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호를 입력해 주세요");
    }
    User user = userRepository.findByUsername(request.username())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다");
    }

    String accessToken = jwtService.generateAccessToken(user.getUsername());
    String refreshToken = jwtService.generateRefreshToken(user.getUsername());
    return new AuthTokens(accessToken, refreshToken, user.getUsername());
  }

  public AuthTokens refresh(String refreshToken) {
    try {
      if (!jwtService.isRefreshToken(refreshToken)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다");
      }

      String username = jwtService.extractUsername(refreshToken);
      String accessToken = jwtService.generateAccessToken(username);
      String newRefreshToken = jwtService.generateRefreshToken(username);
      return new AuthTokens(accessToken, newRefreshToken, username);
    } catch (ExpiredJwtException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다");
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다");
    }
  }
}
