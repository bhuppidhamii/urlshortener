package com.bhuppi.urlshortener.auth.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bhuppi.urlshortener.auth.entity.User;
import com.bhuppi.urlshortener.exception.RateLimitServiceUnavailableException;
import com.bhuppi.urlshortener.service.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())
                || !"/api/shorten".equals(request.getRequestURI())) {

            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof User user)) {

            filterChain.doFilter(request, response);
            return;
        }

        try {

            boolean allowed = rateLimitService.isAllowed(user.getId());

            if (!allowed) {
                response.setStatus(429);
                return;
            }

        } catch (RateLimitServiceUnavailableException e) {

            response.setStatus(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        filterChain.doFilter(request, response);
    }
}