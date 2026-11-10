package com.caresync.dao;

import com.caresync.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Abstract Base DAO providing connection management and parameter mapping utilities.
 */
public abstract class BaseDao<T, ID> implements GenericDao<T, ID> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Obtains a connection from the HikariCP datasource.
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    // =========================================================================
    // Parameter Binding Utilities
    // =========================================================================

    protected void setLongOrNull(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) {
            stmt.setLong(index, value);
        } else {
            stmt.setNull(index, Types.BIGINT);
        }
    }

    protected void setIntegerOrNull(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    protected void setStringOrNull(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value != null && !value.trim().isEmpty()) {
            stmt.setString(index, value.trim());
        } else {
            stmt.setNull(index, Types.VARCHAR);
        }
    }

    protected void setDateOrNull(PreparedStatement stmt, int index, LocalDate value) throws SQLException {
        if (value != null) {
            stmt.setDate(index, java.sql.Date.valueOf(value));
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }

    protected void setTimeOrNull(PreparedStatement stmt, int index, LocalTime value) throws SQLException {
        if (value != null) {
            stmt.setTime(index, Time.valueOf(value));
        } else {
            stmt.setNull(index, Types.TIME);
        }
    }

    protected void setTimestampOrNull(PreparedStatement stmt, int index, Instant value) throws SQLException {
        if (value != null) {
            stmt.setTimestamp(index, Timestamp.from(value));
        } else {
            stmt.setNull(index, Types.TIMESTAMP_WITH_TIMEZONE);
        }
    }

    // =========================================================================
    // ResultSet Extraction Utilities
    // =========================================================================

    protected Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long val = rs.getLong(column);
        return rs.wasNull() ? null : val;
    }

    protected Integer getIntegerOrNull(ResultSet rs, String column) throws SQLException {
        int val = rs.getInt(column);
        return rs.wasNull() ? null : val;
    }

    protected LocalDate getLocalDateOrNull(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date != null ? date.toLocalDate() : null;
    }

    protected LocalTime getLocalTimeOrNull(ResultSet rs, String column) throws SQLException {
        Time time = rs.getTime(column);
        return time != null ? time.toLocalTime() : null;
    }

    protected Instant getInstantOrNull(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}

