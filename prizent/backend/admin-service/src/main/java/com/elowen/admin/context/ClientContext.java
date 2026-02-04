package com.elowen.admin.context;

/**
 * Thread-local context for storing client information during request processing.
 * This allows any service or component to access the current client without passing it around.
 */
public class ClientContext {
    
    private static final ThreadLocal<Integer> clientIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> clientNameHolder = new ThreadLocal<>();
    
    /**
     * Set client ID for current thread
     */
    public static void setClientId(Integer clientId) {
        clientIdHolder.set(clientId);
    }
    
    /**
     * Get client ID from current thread
     */
    public static Integer getClientId() {
        return clientIdHolder.get();
    }
    
    /**
     * Set client name for current thread  
     */
    public static void setClientName(String clientName) {
        clientNameHolder.set(clientName);
    }
    
    /**
     * Get client name from current thread
     */
    public static String getClientName() {
        return clientNameHolder.get();
    }
    
    /**
     * Clear all context data for current thread (important to prevent memory leaks)
     */
    public static void clear() {
        clientIdHolder.remove();
        clientNameHolder.remove();
    }
    
    /**
     * Check if client context is set for current thread
     */
    public static boolean hasClientContext() {
        return clientIdHolder.get() != null;
    }
}