package com.lexflow.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Security for the REST API (/api/**). Checked first because of @Order(1).
     *
     * - HTTP Basic instead of a login form: API clients send credentials with every request
     *   and get 401 (not a redirect to /login) when they are missing.
     * - STATELESS: no HTTP session and no JSESSIONID cookie for API calls.
     * - CSRF disabled: CSRF attacks rely on the browser attaching a session cookie automatically.
     *   Without a session cookie there is nothing to forge, so the token only gets in the way.
     * - Read endpoints are open to USER and ADMIN; every write method requires ADMIN.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().hasRole("ADMIN")
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * Security for the Thymeleaf web UI: form login, session, CSRF protection.
     * Handles every request that the API chain above did not match.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login", "/access-denied").permitAll()

                        .requestMatchers(HttpMethod.GET, "/cases/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/deadlines/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/notes/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/cases/*/documents/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/deadlines/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/notes/*/edit").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/cases/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/deadlines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/notes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/documents/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
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