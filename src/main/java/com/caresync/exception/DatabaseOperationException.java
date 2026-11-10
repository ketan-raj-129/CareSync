package com.caresync.exception;

/**
 * Thrown when a relational database query or transaction fails.
 */
public class DatabaseOperationException extends CareSyncException {

    private final String sqlState;

    public DatabaseOperationException(String message) {
        this(message, null, null);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public DatabaseOperationException(String message, String sqlState, Throwable cause) {
        super("DATABASE_OPERATION_ERROR", message, cause);
        this.sqlState = sqlState;
    }

    public String getSqlState() {
        return sqlState;
    }
}

