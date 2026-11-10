package com.caresync.exception;

/**
 * Thrown when an expected entity cannot be found in persistence.
 */
public class ResourceNotFoundException extends CareSyncException {

    private final String resourceName;
    private final Object identifier;

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s with identifier '%s' was not found", resourceName, identifier));
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Object getIdentifier() {
        return identifier;
    }
}

