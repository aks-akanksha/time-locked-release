package com.example.timelock.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwt;

  public JwtAuthFilter(JwtService jwt) { this.jwt = jwt; }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        Claims claims = Jwts.parserBuilder()
          .setSigningKey(jwt.getKey())
          .requireIssuer(jwt.getIssuer())
          .build()
          .parseClaimsJws(token)
          .getBody();

        String sub = claims.getSubject();
        String role = claims.get("role", String.class);
        var authToken = new UsernamePasswordAuthenticationToken(
          sub, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch (Exception e) {
        // invalid token -> leave context unauthenticated
      }
    }
    chain.doFilter(request, response);
  }
}
