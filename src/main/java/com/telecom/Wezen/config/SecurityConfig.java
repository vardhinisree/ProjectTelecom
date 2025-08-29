package com.telecom.Wezen.config;

import com.telecom.Wezen.jwt.JwtFilter;
import com.telecom.Wezen.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtFilter jwtFilter, UserRepository userRepository) {
        this.jwtFilter = jwtFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF because we use JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/auth/login","/api/plans", "/error").permitAll()  // 🔓 Public endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")          // 👮 ADMIN only
                        .requestMatchers("/plans/**").hasAnyRole("ADMIN", "USER") // 👥 Admin or User
                        .anyRequest().authenticated()                           // 🔒 everything else
                )

                // Use stateless session → JWT only
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            System.out.println("⚠️ Authentication failed: " + authEx.getMessage());
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            String msg = (authEx.getMessage() == null) ? "Unauthorized" : authEx.getMessage();
                            res.getWriter().write(
                                    "{\"status\":401," +
                                            "\"error\":\"Unauthorized\"," +
                                            "\"message\":\"" + msg + "\"," +
                                            "\"path\":\"" + req.getRequestURI() + "\"}"
                            );
                        })
                        .accessDeniedHandler((req, res, accessEx) -> {
                            System.out.println("⛔ Access denied: " + accessEx.getMessage());
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write(
                                    "{\"status\":403," +
                                            "\"error\":\"Forbidden\"," +
                                            "\"message\":\"Access Denied\"," +
                                            "\"path\":\"" + req.getRequestURI() + "\"}"
                            );
                        })
                );

        System.out.println("✅ SecurityConfig loaded successfully");
        return http.build();
    }

    // AuthenticationManager (needed for login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        System.out.println("🔑 AuthenticationManager initialized");
        return config.getAuthenticationManager();
    }

    // Password encoder (BCrypt recommended)
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("🔒 BCryptPasswordEncoder bean created");
        return new BCryptPasswordEncoder();
    }

    // Custom UserDetailsService → loads Users by email/username
    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println("👤 UserDetailsService bean initialized");
        return username -> userRepository.findByMail(username)
                .map(user -> {
                    System.out.println("✅ User found: " + username);
                    return (UserDetails) user;
                })
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }
}
