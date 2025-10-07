package com.example.timelock.entity;

import jakarta.persistence.*;

@Entity @Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true) private String email;
  @Column(nullable = false) private String passwordHash;
  @Column(nullable = false) private String role; // ADMIN, APPROVER, REVIEWER, USER
  @Column(nullable = false) private boolean active = true;

  // getters/setters
  public Long getId() { return id; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
