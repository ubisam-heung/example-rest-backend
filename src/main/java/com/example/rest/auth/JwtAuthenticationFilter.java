package com.example.rest.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    try {
      if (!jwtService.isAccessToken(token)) {
        writeUnauthorized(response, "토큰이 유효하지 않습니다");
        return;
      }

      String username = jwtService.extractUsername(token);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(
            username,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
          );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    } catch (ExpiredJwtException ex) {
      writeUnauthorized(response, "토큰이 만료되었습니다");
      return;
    } catch (Exception ex) {
      writeUnauthorized(response, "토큰이 유효하지 않습니다");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"message\":\"" + message + "\"}");
  }
}
