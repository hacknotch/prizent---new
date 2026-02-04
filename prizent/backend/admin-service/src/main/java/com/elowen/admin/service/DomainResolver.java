package com.elowen.admin.service;

import com.elowen.admin.entity.Client;
import com.elowen.admin.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for resolving client information from domain names.
 * Handles subdomain extraction and client lookup from database.
 */
@Service
public class DomainResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(DomainResolver.class);
    
    @Autowired
    private ClientRepository clientRepository;
    
    /**
     * Resolve client ID from domain name
     * 
     * Examples:
     * - shaktibrands.prizent.com -> looks up client with domain "shaktibrands"
     * - api.prizent.com -> looks up client with domain "api" 
     * - localhost:8080 -> returns null (no client for localhost)
     * 
     * @param serverName The server name from HTTP request (e.g., "shaktibrands.prizent.com")
     * @return Client ID if found and enabled, null otherwise
     */
    public Integer resolveClientFromDomain(String serverName) {
        if (serverName == null || serverName.trim().isEmpty()) {
            logger.warn("Empty or null server name provided");
            return null;
        }
        
        try {
            // Extract subdomain from server name
            String subdomain = extractSubdomain(serverName);
            
            if (subdomain == null) {
                logger.debug("No subdomain found for server: {}", serverName);
                return null;
            }
            
            logger.debug("Extracted subdomain: {} from server: {}", subdomain, serverName);
            
            // Look up client by domain
            Optional<Client> clientOpt = clientRepository.findByClientDomainAndEnabled(subdomain, true);
            
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                logger.debug("Found active client: {} (ID: {}) for domain: {}", 
                            client.getName(), client.getId(), subdomain);
                return client.getId();
            } else {
                logger.warn("No active client found for domain: {}", subdomain);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error resolving client from domain {}: {}", serverName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract subdomain from server name
     * 
     * Examples:
     * - "shaktibrands.prizent.com" -> "shaktibrands"
     * - "shaktibrands.localhost" -> "shaktibrands" (for testing)
     * - "api.prizent.com" -> "api"
     * - "prizent.com" -> null (no subdomain)
     * - "localhost" -> null (no subdomain)
     * - "localhost:8080" -> null (no subdomain)
     * 
     * @param serverName Full server name
     * @return Subdomain or null if no subdomain found
     */
    private String extractSubdomain(String serverName) {
        if (serverName == null) {
            return null;
        }
        
        // Remove port if present (e.g., "localhost:8080" -> "localhost")
        String cleanServerName = serverName.split(":")[0];
        
        // Skip plain localhost and IP addresses
        if (cleanServerName.equals("localhost") || isIpAddress(cleanServerName)) {
            return null;
        }
        
        // Split by dots
        String[] parts = cleanServerName.split("\\.");
        
        // For domains with dots, need at least 2 parts for subdomain
        // Examples: 
        // - ["shaktibrands", "localhost"] -> "shaktibrands" (for testing)
        // - ["shaktibrands", "prizent", "com"] -> "shaktibrands" (production)
        if (parts.length >= 2) {
            return parts[0]; // Return first part as subdomain
        }
        
        return null; // No subdomain found
    }
    
    /**
     * Check if a string looks like an IP address
     */
    private boolean isIpAddress(String str) {
        return str.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }
    
    /**
     * Get client by ID (helper method for getting full client info)
     */
    public Optional<Client> getClientById(Integer clientId) {
        if (clientId == null) {
            return Optional.empty();
        }
        
        try {
            return clientRepository.findById(clientId);
        } catch (Exception e) {
            logger.error("Error getting client by ID {}: {}", clientId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}