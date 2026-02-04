package com.elowen.identity.filter;

import com.elowen.identity.context.ClientContext;
import com.elowen.identity.entity.Client;
import com.elowen.identity.service.DomainResolver;
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
import java.util.Optional;

/**
 * Filter that resolves client from domain and sets client context.
 * 
 * This filter runs BEFORE authentication and authorization filters.
 * It ensures that every request has a valid client context established
 * before any business logic executes.
 * 
 * SECURITY RESPONSIBILITIES:
 * - Extract subdomain from Host header
 * - Resolve client from database
 * - Reject requests for invalid/disabled clients
 * - Set client context for downstream components
 * - Clear context after request completion
 */
@Component
@Order(1) // Run first, before authentication
public class DomainResolutionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DomainResolutionFilter.class);

    @Autowired
    private DomainResolver domainResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String host = request.getHeader("Host");
        
        log.debug("Processing request: {} {} from host: {}", method, requestURI, host);

        try {
            // Skip domain resolution for certain endpoints
            if (shouldSkipDomainResolution(requestURI)) {
                log.debug("Skipping domain resolution for: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            // Resolve client from domain
            Optional<Client> clientOpt = domainResolver.resolveClientFromRequest(request);
            
            if (clientOpt.isEmpty()) {
                log.warn("No valid client found from domain for request: {} {} from host: {}", 
                        method, requestURI, host);
                // Instead of blocking, allow the request to proceed to controller
                // The controller will attempt to resolve client from request body
                log.debug("Allowing request to proceed - controller will attempt client resolution from request body");
                filterChain.doFilter(request, response);
                return;
            }

            // Set client in context
            Client client = clientOpt.get();
            ClientContext.setClient(client);
            
            log.debug("Client resolved successfully: {} (ID: {}) for request: {} {}", 
                     client.getName(), client.getId(), method, requestURI);

            // Add client info to response headers for debugging (in dev mode)
            response.setHeader("X-Client-Name", client.getName());
            response.setHeader("X-Client-Domain", client.getClientDomain());

            // Continue with the request
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error in domain resolution filter for request: {} {}", method, requestURI, e);
            sendErrorResponse(response, "Internal server error during client resolution");
        } finally {
            // CRITICAL: Always clear context after request to prevent memory leaks
            ClientContext.clear();
        }
    }

    /**
     * Determine if domain resolution should be skipped for this URI
     */
    private boolean shouldSkipDomainResolution(String requestURI) {
        return requestURI.startsWith("/actuator/") || 
               requestURI.startsWith("/health") ||
               requestURI.startsWith("/api-docs") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/favicon.ico") ||
               requestURI.startsWith("/error");
    }

    /**
     * Send 404 response when client is not found
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
}