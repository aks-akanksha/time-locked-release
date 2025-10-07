package com.example.timelock.entity;

import jakarta.persistence.*;

@Entity @Table(name = "route_scopes")
public class RouteScope {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false) private String routePattern;
  @Column(nullable = false) private String method;
  @Column(nullable = false) private String requiredRole;

  // getters/setters
  public Long getId() { return id; }
  public String getRoutePattern() { return routePattern; }
  public void setRoutePattern(String routePattern) { this.routePattern = routePattern; }
  public String getMethod() { return method; }
  public void setMethod(String method) { this.method = method; }
  public String getRequiredRole() { return requiredRole; }
  public void setRequiredRole(String requiredRole) { this.requiredRole = requiredRole; }
}
