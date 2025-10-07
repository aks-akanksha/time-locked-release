package com.example.timelock.policy;

import com.example.timelock.repo.RouteScopeRepository;
import com.example.timelock.entity.RouteScope;
import org.springframework.stereotype.Service;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.http.server.PathContainer;

import java.util.Comparator;

@Service
public class RouteScopeService {
  private final RouteScopeRepository repo;
  private final PathPatternParser parser = new PathPatternParser();

  public RouteScopeService(RouteScopeRepository repo) { this.repo = repo; }

  public String requiredRoleFor(String method, String path) {
    var requestPath = PathContainer.parsePath(path);
    return repo.findAll().stream()
      .filter(r -> method.equalsIgnoreCase(r.getMethod()))
      .filter(r -> {
        PathPattern p = parser.parse(r.getRoutePattern()); // e.g. /api/v1/releases/{id}/actions/schedule
        return p.matches(requestPath);
      })
      .sorted(Comparator.comparingInt((RouteScope r) -> r.getRoutePattern().length()).reversed())
      .map(RouteScope::getRequiredRole)
      .findFirst()
      .orElse(null);
  }
}
