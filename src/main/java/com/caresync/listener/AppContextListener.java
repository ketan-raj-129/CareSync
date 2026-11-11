package com.caresync.listener;

import com.caresync.config.DatabaseConfig;
import com.caresync.dao.AppointmentDaoImpl;
import com.caresync.dao.DoctorDaoImpl;
import com.caresync.dao.NotificationDao;
import com.caresync.dao.NotificationDaoImpl;
import com.caresync.dao.NotificationDeliveryDao;
import com.caresync.dao.NotificationDeliveryDaoImpl;
import com.caresync.dao.NotificationPreferenceDao;
import com.caresync.dao.NotificationPreferenceDaoImpl;
import com.caresync.dao.PatientDaoImpl;
import com.caresync.notification.EmailNotificationProvider;
import com.caresync.notification.NotificationProvider;
import com.caresync.notification.NotificationTemplateService;
import com.caresync.notification.NotificationTemplateServiceImpl;
import com.caresync.service.AdminSettingsServiceImpl;
import com.caresync.service.NotificationDispatcher;
import com.caresync.service.NotificationDispatcherImpl;
import com.caresync.service.NotificationEventService;
import com.caresync.service.NotificationEventServiceImpl;
import com.caresync.service.NotificationPreferenceService;
import com.caresync.service.NotificationPreferenceServiceImpl;
import com.caresync.service.NotificationScheduler;
import com.caresync.service.NotificationSchedulerImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application lifecycle listener that initializes connection pools and
 * background notification schedulers on startup, ensuring graceful cleanup on undeploy.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);
    private NotificationScheduler notificationScheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing CareSync application context...");
        if (DatabaseConfig.isConfigured()) {
            try {
                DatabaseConfig.getDataSource();
                logger.info("CareSync HikariCP connection pool initialized successfully.");

                // Initialize NotificationScheduler background engine
                try {
                    NotificationDeliveryDao deliveryDao = new NotificationDeliveryDaoImpl();
                    NotificationDao notifDao = new NotificationDaoImpl();
                    NotificationPreferenceDao prefDao = new NotificationPreferenceDaoImpl();
                    NotificationPreferenceService prefService = new NotificationPreferenceServiceImpl(prefDao);
                    NotificationTemplateService templateService = new NotificationTemplateServiceImpl();
                    NotificationProvider emailProvider = new EmailNotificationProvider();

                    NotificationDispatcher dispatcher = new NotificationDispatcherImpl(deliveryDao, emailProvider);
                    NotificationEventService eventService = new NotificationEventServiceImpl(notifDao, deliveryDao, prefService, templateService);

                    notificationScheduler = new NotificationSchedulerImpl(
                            dispatcher,
                            eventService,
                            new AppointmentDaoImpl(),
                            new PatientDaoImpl(),
                            new DoctorDaoImpl(),
                            new AdminSettingsServiceImpl()
                    );
                    notificationScheduler.start();
                    sce.getServletContext().setAttribute("notificationScheduler", notificationScheduler);
                    logger.info("CareSync NotificationScheduler background worker started.");
                } catch (Exception ex) {
                    logger.warn("Could not start NotificationScheduler background engine: {}", ex.getMessage());
                }
            } catch (Exception e) {
                logger.error("Failed to initialize database connection pool: {}", e.getMessage(), e);
            }
        } else {
            logger.info("CareSync starting in standalone/unconfigured mode. Database features pending valid .env credentials.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying CareSync application context...");
        if (notificationScheduler != null) {
            try {
                notificationScheduler.stop();
            } catch (Exception ex) {
                logger.warn("Error stopping notificationScheduler during undeploy: {}", ex.getMessage());
            }
        }
        DatabaseConfig.shutdown();
        logger.info("CareSync application context cleanly destroyed.");
    }
}
