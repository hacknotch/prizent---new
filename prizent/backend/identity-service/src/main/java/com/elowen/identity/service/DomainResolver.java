package com.elowen.identity.service;

import com.elowen.identity.entity.Client;
import com.elowen.identity.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Service responsible for resolving client from domain/subdomain.
 * 
 * Core functionality:
 * - Extract subdomain from Host header
 * - Resolve client from subdomain
 * - Provide client context for the application
 * 
 * Security: Only enabled clients are resolved
 */
@Component
public class DomainResolver {

    private static final Logger log = LoggerFactory.getLogger(DomainResolver.class);

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Extract subdomain from Host header
     * Example: shaktibrands.prizent.com -> shaktibrands
     *          shaktibrands.localhost -> shaktibrands (for testing)
     *          localhost:8080 -> null
     *          prizent.com -> null
     */
    public String extractSubdomain(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            log.warn("Host header is missing in request");
            return null;
        }

        log.debug("Extracting subdomain from host: {}", host);

        // Remove port if present (e.g., localhost:8080 -> localhost)
        if (host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }

        // Skip plain localhost and IP addresses
        if (host.equals("localhost")) {
            log.debug("Skipping plain localhost - no subdomain");
            return null;
        }

        // Split by dots
        String[] parts = host.split("\\.");
        
        // For domains with dots, need at least 2 parts for subdomain
        // Examples: 
        // - ["shaktibrands", "localhost"] -> "shaktibrands" (for testing)
        // - ["shaktibrands", "prizent", "com"] -> "shaktibrands" (production)
        if (parts.length >= 2) {
            String subdomain = parts[0];
            log.debug("Extracted subdomain: {}", subdomain);
            return subdomain;
        }

        log.debug("No subdomain found in host: {}", host);
        return null;
    }

    /**
     * Resolve client from subdomain
     * Only returns enabled clients
     */
    public Optional<Client> resolveClient(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            log.warn("Cannot resolve client: subdomain is null or empty");
            return Optional.empty();
        }

        log.debug("Resolving client for subdomain: {}", subdomain);
        
        Optional<Client> client = clientRepository.findByClientDomainAndEnabled(subdomain, true);
        
        if (client.isPresent()) {
            log.debug("Found client: {} (ID: {}) for subdomain: {}", 
                     client.get().getName(), client.get().getId(), subdomain);
        } else {
            log.warn("No active client found for subdomain: {}", subdomain);
        }
        
        return client;
    }

    /**
     * Complete domain resolution from request
     * Main entry point for resolving client from HTTP request
     */
    public Optional<Client> resolveClientFromRequest(HttpServletRequest request) {
        String subdomain = extractSubdomain(request);
        return resolveClient(subdomain);
    }

    /**
     * Validate if subdomain is available (for client registration)
     */
    public boolean isSubdomainAvailable(String subdomain) {
        if (subdomain == null || subdomain.isEmpty()) {
            return false;
        }
        return !clientRepository.existsByClientDomain(subdomain);
    }
}