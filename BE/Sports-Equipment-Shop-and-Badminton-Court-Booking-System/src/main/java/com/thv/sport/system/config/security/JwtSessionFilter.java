package com.thv.sport.system.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtSessionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String jwt = (String) session.getAttribute("jwt");

            if (jwt != null && request.getHeader("Authorization") == null) {
                // Gắn JWT từ session vào header Authorization
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getHeader(String name) {
                        if ("Authorization".equalsIgnoreCase(name)) {
                            return "Bearer " + jwt;
                        }
                        return super.getHeader(name);
                    }
                };
                filterChain.doFilter(wrapper, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
