package com.example.timelock.policy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.timelock.entity.RouteScope;

@Component
public class PolicyInterceptor implements HandlerInterceptor {

  private final RouteScopeService routeScopeService;

  public PolicyInterceptor(RouteScopeService routeScopeService) {
    this.routeScopeService = routeScopeService;
  }

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    if (!(handler instanceof HandlerMethod)) return true;

    String method = req.getMethod();
    String path = req.getRequestURI();
    String requiredRole = routeScopeService.requiredRoleFor(method, path);

    if (requiredRole == null) return true;

    var auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAnonymous = (auth == null) || !auth.isAuthenticated()
        || "anonymousUser".equals(String.valueOf(auth.getPrincipal()));
    if (isAnonymous) {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
      return false;
    }

    String needed = "ROLE_" + requiredRole;
    boolean hasRoleOrAdmin = auth.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .anyMatch(g -> g.equals(needed) || g.equals("ROLE_ADMIN"));
    if (!hasRoleOrAdmin) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
      return false;
    }
    return true;
  }
}
