package com.example.timelock.security;

import com.example.timelock.security.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final UserDetailsService uds;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService uds) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.uds = uds;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/auth/login", "/api/v1/hello").permitAll()
        .anyRequest().authenticated()
      )
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
      // .anonymous(anon -> anon.disable());
      
    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(provider);
  }

  @Bean
  org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
    var cfg = new org.springframework.web.cors.CorsConfiguration();
    cfg.setAllowedOrigins(java.util.List.of("http://localhost:3000", "http://localhost:5173")); // your UI(s)
    cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(java.util.List.of("Authorization","Content-Type"));
    var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

}
