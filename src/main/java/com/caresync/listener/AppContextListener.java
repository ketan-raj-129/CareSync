package com.caresync.listener;

import com.caresync.config.DatabaseConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application lifecycle listener that initializes connection pools on startup
 * and ensures graceful pool shutdown when Tomcat undeploys the application.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing CareSync application context...");
        if (DatabaseConfig.isConfigured()) {
            try {
                DatabaseConfig.getDataSource();
                logger.info("CareSync HikariCP connection pool initialized successfully.");
            } catch (Exception e) {
                logger.error("Failed to initialize database connection pool: {}", e.getMessage(), e);
            }
        } else {
            logger.info("CareSync starting in standalone/unconfigured mode. Database features pending valid .env credentials.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying CareSync application context. Shutting down connection pools...");
        DatabaseConfig.shutdown();
        logger.info("CareSync application context cleanly destroyed.");
    }
}

