package com.elowen.admin.filter;

import com.elowen.admin.context.ClientContext;
import com.elowen.admin.service.DomainResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to resolve client from subdomain and set in ClientContext
 * MUST run before JWT authentication to establish tenant context
 */
@Component
@Order(1) // Run first, before JWT authentication
public class DomainResolutionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(DomainResolutionFilter.class);

    @Autowired
    private DomainResolver domainResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract domain from Host header first, fallback to server name
            String hostHeader = request.getHeader("Host");
            String serverName = hostHeader != null ? hostHeader : request.getServerName();
            logger.debug("Processing request for server: {} (Host header: {})", serverName, hostHeader);

            // Skip domain resolution for certain endpoints
            String requestURI = request.getRequestURI();
            if (shouldSkipDomainResolution(requestURI)) {
                logger.debug("Skipping domain resolution for: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // Get client from domain resolution
            Integer clientId = domainResolver.resolveClientFromDomain(serverName);
            
            if (clientId != null) {
                // Set client context for this request thread
                ClientContext.setClientId(clientId);
                logger.debug("Set client context: {} for domain: {}", clientId, serverName);
            } else {
                // For localhost/development: allow request to proceed
                // Client context will be extracted from JWT token in JwtAuthenticationFilter
                logger.debug("No client from domain: {} - will extract from JWT token", serverName);
            }
            
            // Always continue with request - JWT filter will handle client validation
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in domain resolution: {}", e.getMessage(), e);
            sendErrorResponse(response, "Domain resolution failed");
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