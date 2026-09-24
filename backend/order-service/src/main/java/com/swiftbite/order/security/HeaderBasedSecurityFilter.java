package com.swiftbite.order.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HeaderBasedSecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HeaderBasedSecurityFilter.class);

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String rolesHeader = request.getHeader(USER_ROLES_HEADER);

        if (StringUtils.hasText(userIdHeader)) {
            try {
                Long userId = Long.parseLong(userIdHeader);

                List<SimpleGrantedAuthority> authorities;
                if (StringUtils.hasText(rolesHeader)) {
                    authorities = Arrays.stream(rolesHeader.split(","))
                            .map(String::trim)
                            .filter(StringUtils::hasText)
                            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                } else {
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                }

                AuthenticatedUser principal = new AuthenticatedUser(userId, userEmail, authorities);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authenticated user {} with roles {}", userId, authorities);

            } catch (NumberFormatException ex) {
                logger.warn("Invalid X-User-Id header value: {}", userIdHeader);
            }
        }

        filterChain.doFilter(request, response);
    }
}
