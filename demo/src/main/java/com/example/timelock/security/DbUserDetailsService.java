package com.example.timelock.security;

import com.example.timelock.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Service("dbUserDetailsService")
@Primary
public class DbUserDetailsService implements UserDetailsService {
  private final UserRepository repo;

  public DbUserDetailsService(UserRepository repo) { this.repo = repo; }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u = repo.findByEmailAndActiveTrue(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
      u.getEmail(), u.getPasswordHash(), List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
    );
  }
}
