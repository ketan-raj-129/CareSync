package com.caresync.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages the HikariCP database connection pool singleton, specifically
 * optimized for Neon Cloud Serverless PostgreSQL (Scale-to-Zero and cold-start recovery).
 */
public final class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile HikariDataSource dataSource = null;
    private static final Object LOCK = new Object();

    private DatabaseConfig() {}

    /**
     * Checks if mandatory database configuration variables are provided.
     */
    public static boolean isConfigured() {
        String host = EnvConfig.get("DB_HOST");
        String user = EnvConfig.get("DB_USER");
        String password = EnvConfig.get("DB_PASSWORD");
        return host != null && !host.trim().isEmpty() && !host.contains("your-neon-endpoint") &&
               user != null && !user.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() && !password.contains("your_secure_password");
    }

    /**
     * Initializes or returns the existing HikariDataSource singleton.
     */
    public static HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    dataSource = initDataSource();
                }
            }
        }
        return dataSource;
    }

    private static HikariDataSource initDataSource() {
        if (!isConfigured()) {
            logger.warn("Neon PostgreSQL connection parameters are not fully configured. Database operations will be disabled.");
            return null;
        }

        String host = EnvConfig.get("DB_HOST");
        int port = EnvConfig.getInt("DB_PORT", 5432);
        String dbName = EnvConfig.get("DB_NAME", "caresync_db");
        String user = EnvConfig.get("DB_USER", "caresync_app");
        String password = EnvConfig.get("DB_PASSWORD", "");
        String sslMode = EnvConfig.get("DB_SSLMODE", "require");

        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?sslmode=%s", host, port, dbName, sslMode);

        logger.info("Initializing HikariCP pool for Neon PostgreSQL: jdbc:postgresql://{}:{}/{}", host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("CareSync-HikariPool");

        // Neon Serverless Scale-to-Zero Optimization
        // minimumIdle = 0 allows all idle connections to close so Neon compute can suspend
        int minIdle = EnvConfig.getInt("DB_POOL_MIN_IDLE", 0);
        int maxPoolSize = EnvConfig.getInt("DB_POOL_MAX_SIZE", 10);
        long idleTimeout = EnvConfig.getInt("DB_POOL_IDLE_TIMEOUT_MS", 600000); // 10 mins
        long maxLifetime = EnvConfig.getInt("DB_POOL_MAX_LIFETIME_MS", 1800000); // 30 mins
        long connTimeout = EnvConfig.getInt("DB_POOL_CONN_TIMEOUT_MS", 30000);   // 30s for Neon cold start

        config.setMinimumIdle(minIdle);
        config.setMaximumPoolSize(maxPoolSize);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setConnectionTimeout(connTimeout);
        config.setValidationTimeout(5000);

        // DO NOT enable aggressive keepalive as it prevents Neon scale-to-zero

        // Performance & PostgreSQL driver optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    /**
     * Borrows an active connection from the pool.
     * Callers must close the connection (e.g. via try-with-resources) to return it to the pool.
     */
    public static Connection getConnection() throws SQLException {
        HikariDataSource ds = getDataSource();
        if (ds == null) {
            throw new SQLException("CareSync Database is not configured. Please supply valid Neon credentials in .env");
        }
        return ds.getConnection();
    }

    /**
     * Gracefully shuts down the HikariCP connection pool on application undeployment.
     */
    public static void shutdown() {
        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                logger.info("Closing HikariCP database connection pool...");
                dataSource.close();
                dataSource = null;
            }
        }
    }
}

