package com.telecom.Wezen.jwt;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    // ✅ Define which endpoints don’t need JWT
    private final RequestMatcher publicEndpoints = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/auth/**"),
            new AntPathRequestMatcher("/error"),
            new AntPathRequestMatcher("/api/plans/**"),
            new AntPathRequestMatcher("/api/recharges/**"),
            new AntPathRequestMatcher("/api/users/**")
    );

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        try {
            final String token = authHeader.substring(7);
            final String email = jwtUtil.extractEmail(token);
            final String role = jwtUtil.extractRole(token);
            final long planId = jwtUtil.extractPlanId(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Make planId available for controllers/services
                    request.setAttribute("planId", planId);
                }
            }
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    // ✅ Spring will skip this filter for auth/register/login and OPTIONS
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // preflight
        boolean skip = publicEndpoints.matches(request);
        if (skip) {
            log.debug("JwtFilter SKIPPED for {}", request.getRequestURI());
        } else {
            log.debug("JwtFilter APPLIES to {}", request.getRequestURI());
        }
        return skip;
    }
}
