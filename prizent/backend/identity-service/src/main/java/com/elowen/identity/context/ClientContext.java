package com.elowen.identity.context;

import com.elowen.identity.entity.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local context to hold client information for the current request.
 * 
 * This provides a secure way to access client information throughout
 * the request lifecycle without passing client_id as parameters.
 * 
 * SECURITY NOTES:
 * - Uses ThreadLocal for request isolation
 * - Must be cleared after each request to prevent memory leaks
 * - Only populated by DomainResolutionFilter
 */
public class ClientContext {

    private static final Logger log = LoggerFactory.getLogger(ClientContext.class);
    
    private static final ThreadLocal<Client> clientThreadLocal = new ThreadLocal<>();

    /**
     * Set client for current thread/request
     * Should only be called by DomainResolutionFilter
     */
    public static void setClient(Client client) {
        if (client != null) {
            log.debug("Setting client context: {} (ID: {}, Domain: {})", 
                     client.getName(), client.getId(), client.getClientDomain());
        }
        clientThreadLocal.set(client);
    }

    /**
     * Get client for current thread/request
     * Returns null if no client is set
     */
    public static Client getClient() {
        Client client = clientThreadLocal.get();
        if (client == null) {
            log.warn("No client found in context - this should not happen in authenticated requests");
        }
        return client;
    }

    /**
     * Get client ID for current thread/request
     * Primary method used by services and repositories
     */
    public static Integer getClientId() {
        Client client = getClient();
        return client != null ? client.getId() : null;
    }

    /**
     * Get client domain for current thread/request
     * Useful for logging and debugging
     */
    public static String getClientDomain() {
        Client client = getClient();
        return client != null ? client.getClientDomain() : null;
    }

    /**
     * Get client name for current thread/request
     * Useful for logging and response headers
     */
    public static String getClientName() {
        Client client = getClient();
        return client != null ? client.getName() : null;
    }

    /**
     * Clear client context
     * MUST be called after each request to prevent memory leaks
     */
    public static void clear() {
        Client client = clientThreadLocal.get();
        if (client != null) {
            log.debug("Clearing client context for: {} (ID: {})", 
                     client.getName(), client.getId());
        }
        clientThreadLocal.remove();
    }

    /**
     * Check if client is set in context
     * Useful for conditional logic
     */
    public static boolean hasClient() {
        return clientThreadLocal.get() != null;
    }

    /**
     * Get client context safely with fallback
     * Returns client if available, null otherwise (no exceptions)
     */
    public static Client getClientSafely() {
        try {
            return clientThreadLocal.get();
        } catch (Exception e) {
            log.error("Error accessing client context", e);
            return null;
        }
    }
}