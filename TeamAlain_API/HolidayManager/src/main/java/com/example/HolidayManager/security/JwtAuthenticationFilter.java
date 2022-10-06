package com.example.HolidayManager.security;

import com.example.HolidayManager.util.exceptions.JwtAuthenticationException;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jwt filter for every request to get the user's authentication
 *
 * @author cvisan
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest servletRequest, @NotNull HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = jwtTokenService.getAuthentication(servletRequest);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (final JwtAuthenticationException ex) {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error parsing JWT Token");
        }
    }
}
