package com.example.timelock.policy;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final PolicyInterceptor policyInterceptor;

  public WebConfig(PolicyInterceptor policyInterceptor) {
    this.policyInterceptor = policyInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(policyInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/auth/**",
            "/api/v1/hello",     // <-- public, bypass the interceptor
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**"
        );
  }
}