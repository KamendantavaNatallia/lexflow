package com.lexflow.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/access-denied").permitAll()

                        // ADMIN-only GET pages
                        .requestMatchers(HttpMethod.GET, "/cases/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/deadlines/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/notes/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/documents/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/deadlines/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/notes/*/edit").hasRole("ADMIN")

                        // ADMIN-only write operations
                        .requestMatchers(HttpMethod.POST, "/cases/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/deadlines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/documents/**").hasRole("ADMIN")

                        // Everything else requires login
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}