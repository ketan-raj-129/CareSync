package com.caresync.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when domain or input validation rules fail.
 */
public class ValidationException extends CareSyncException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        this(message, Collections.emptyMap());
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

