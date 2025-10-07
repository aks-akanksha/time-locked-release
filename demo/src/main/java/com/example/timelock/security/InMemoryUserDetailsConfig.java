package com.example.timelock.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile("inmem")
public class InMemoryUserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.withUsername("admin@example.com")
                .password("{noop}password")  // {noop} means no encoding for demo
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
