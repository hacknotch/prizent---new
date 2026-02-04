package com.elowen.admin.filter;

import com.elowen.admin.context.ClientContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to set client context (simplified - no domain resolution)
 * Client ID will be extracted from JWT token in JwtAuthenticationFilter
 */
@Component
@Order(1) // Run first, before JWT authentication
public class DomainResolutionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(DomainResolutionFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            String requestURI = request.getRequestURI();
            logger.debug("Processing request: {}", requestURI);

            // Skip for public endpoints
            if (shouldSkipDomainResolution(requestURI)) {
                logger.debug("Skipping for: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // Client context will be extracted from JWT token in JwtAuthenticationFilter
            // This filter just ensures context is cleared after request
            
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in domain resolution filter: {}", e.getMessage(), e);
            sendErrorResponse(response, "Request processing failed");
        } finally {
            // Always clear context after request
            ClientContext.clear();
        }
    }

    /**
     * Send 404 response when client not found
     */
    private void sendClientNotFoundResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Client not found or inactive\",\"code\":\"CLIENT_NOT_FOUND\"}");
    }

    /**
     * Send 500 response for internal errors
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\":\"%s\",\"code\":\"INTERNAL_ERROR\"}", message));
    }
    /**
     * Check if domain resolution should be skipped for this endpoint
     */
    private boolean shouldSkipDomainResolution(String requestURI) {
        // Skip actuator endpoints
        if (requestURI.startsWith("/actuator/")) {
            return true;
        }
        
        // Skip health check endpoints
        if (requestURI.equals("/health") || requestURI.equals("/ping")) {
            return true;
        }
        
        // Skip error endpoints
        if (requestURI.equals("/error")) {
            return true;
        }
        
        return false;
    }}