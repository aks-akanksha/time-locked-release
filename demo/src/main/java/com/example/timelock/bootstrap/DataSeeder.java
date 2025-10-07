package com.example.timelock.bootstrap;

import com.example.timelock.entity.RouteScope;
import com.example.timelock.entity.User;
import com.example.timelock.repo.RouteScopeRepository;
import com.example.timelock.repo.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

  @Bean
  ApplicationRunner seed(UserRepository users, RouteScopeRepository routes, PasswordEncoder enc) {
    return args -> {
      users.findByEmailAndActiveTrue("admin@example.com").orElseGet(() -> {
        var u = new User();
        u.setEmail("admin@example.com");
        u.setPasswordHash(enc.encode("admin123"));
        u.setRole("ADMIN");
        u.setActive(true);
        return users.save(u);
      });

      users.findByEmailAndActiveTrue("approver@example.com").orElseGet(() -> {
        var u = new User();
        u.setEmail("approver@example.com");
        u.setPasswordHash(enc.encode("approver123"));
        u.setRole("APPROVER");
        u.setActive(true);
        return users.save(u);
      });

      users.findByEmailAndActiveTrue("user@example.com").orElseGet(() -> {
        var u = new User();
        u.setEmail("user@example.com");
        u.setPasswordHash(enc.encode("user123"));
        u.setRole("USER");
        u.setActive(true);
        return users.save(u);
      });

      // if (routes.count() == 0) {
      //   // public (still bypassed by WebConfig)
      //   var rHello = new RouteScope();
      //   rHello.setMethod("GET");
      //   rHello.setRoutePattern("/actuator/health");
      //   rHello.setRequiredRole("USER");
      //   routes.save(rHello);

      //   // generic releases (list/create) -> USER
      //   var rReleasesGet = new RouteScope();
      //   rReleasesGet.setMethod("GET");
      //   rReleasesGet.setRoutePattern("/api/v1/releases/");
      //   rReleasesGet.setRequiredRole("USER");
      //   routes.save(rReleasesGet);

      //   var rReleasesPost = new RouteScope();
      //   rReleasesPost.setMethod("POST");
      //   rReleasesPost.setRoutePattern("/api/v1/releases/");
      //   rReleasesPost.setRequiredRole("USER");
      //   routes.save(rReleasesPost);

      //   // action-specific (more specific path segments) override generic
      //   var rApprove = new RouteScope();
      //   rApprove.setMethod("POST");
      //   rApprove.setRoutePattern("/api/v1/releases/") ; // prefix match + longest pattern logic applies
      //   rApprove.setRequiredRole("APPROVER");
      //   routes.save(rApprove);

      //   var rSchedule = new RouteScope();
      //   rSchedule.setMethod("POST");
      //   rSchedule.setRoutePattern("/api/v1/releases/"); // schedule path starts with same prefix
      //   rSchedule.setRequiredRole("ADMIN");
      //   routes.save(rSchedule);

      //   var rExecute = new RouteScope();
      //   rExecute.setMethod("POST");
      //   rExecute.setRoutePattern("/api/v1/releases/"); // execute path starts with same prefix
      //   rExecute.setRequiredRole("ADMIN");
      //   routes.save(rExecute);
      // }
      if (routes.count() == 0) {
        // list + create (USER)
        routes.save(row("GET", "/api/v1/releases", "USER"));
        routes.save(row("POST", "/api/v1/releases", "USER"));

        // actions
        routes.save(row("POST", "/api/v1/releases/{id}/actions/schedule", "ADMIN"));
        routes.save(row("POST", "/api/v1/releases/{id}/actions/approve", "APPROVER"));
        routes.save(row("POST", "/api/v1/releases/{id}/actions/execute", "ADMIN"));
      }
    };
  }

  private RouteScope row(String method, String routePattern, String requiredRole) {
    RouteScope routeScope = new RouteScope();
    routeScope.setMethod(method);
    routeScope.setRoutePattern(routePattern);
    routeScope.setRequiredRole(requiredRole);
    return routeScope;
  }
}