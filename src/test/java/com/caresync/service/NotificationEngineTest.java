package com.caresync.service;

import com.caresync.dao.AppointmentDao;
import com.caresync.dao.DoctorDao;
import com.caresync.dao.NotificationDao;
import com.caresync.dao.NotificationDeliveryDao;
import com.caresync.dao.NotificationPreferenceDao;
import com.caresync.dao.PatientDao;
import com.caresync.dto.NotificationDto;
import com.caresync.dto.NotificationPreferenceDto;
import com.caresync.model.Appointment;
import com.caresync.model.AppointmentStatus;
import com.caresync.model.Doctor;
import com.caresync.model.Notification;
import com.caresync.model.NotificationChannel;
import com.caresync.model.NotificationDelivery;
import com.caresync.model.NotificationDeliveryStatus;
import com.caresync.model.NotificationPreference;
import com.caresync.model.NotificationType;
import com.caresync.model.Patient;
import com.caresync.notification.MockNotificationProvider;
import com.caresync.notification.NotificationTemplateService;
import com.caresync.notification.NotificationTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CareSync Phase 7 Notification & Communication Engine Tests")
class NotificationEngineTest {

    private InMemoryNotificationDao notificationDao;
    private InMemoryNotificationDeliveryDao deliveryDao;
    private InMemoryNotificationPreferenceDao preferenceDao;
    private MockNotificationProvider mockEmailProvider;
    private NotificationTemplateService templateService;

    private NotificationService notificationService;
    private NotificationPreferenceService preferenceService;
    private NotificationDispatcherImpl dispatcher;
    private NotificationEventService eventService;

    private static final Long PATIENT_USER_ID = 101L;
    private static final Long DOCTOR_USER_ID = 202L;

    @BeforeEach
    void setUp() {
        notificationDao = new InMemoryNotificationDao();
        deliveryDao = new InMemoryNotificationDeliveryDao();
        preferenceDao = new InMemoryNotificationPreferenceDao();
        mockEmailProvider = new MockNotificationProvider();
        templateService = new NotificationTemplateServiceImpl();

        notificationService = new NotificationServiceImpl(notificationDao);
        preferenceService = new NotificationPreferenceServiceImpl(preferenceDao);
        dispatcher = new NotificationDispatcherImpl(deliveryDao, mockEmailProvider);
        eventService = new NotificationEventServiceImpl(notificationDao, deliveryDao, preferenceService, templateService);
    }

    // =========================================================================
    // 1. Template Rendering & Brand Identity Tests
    // =========================================================================

    @Test
    @DisplayName("Email templates contain CareSync brand palette and required metadata")
    void testEmailTemplateBrandingAndContent() {
        Patient pt = createTestPatient(PATIENT_USER_ID, "John Doe", "john@example.com");
        Doctor doc = createTestDoctor(DOCTOR_USER_ID, "Dr. Sarah Smith", "sarah@caresync.com");
        Appointment apt = createTestAppointment(1L, "APT-12345678", pt.getId(), doc.getId(), LocalDate.now().plusDays(1), LocalTime.of(10, 30));

        var rendered = templateService.renderAppointmentBookedPatient(apt, pt, doc);

        assertNotNull(rendered.subject());
        assertTrue(rendered.subject().contains("APT-12345678"));
        assertTrue(rendered.htmlBody().contains("#176E65"), "HTML must contain CareSync primary teal");
        assertTrue(rendered.htmlBody().contains("#E9F3F2"), "HTML must contain CareSync soft teal");
        assertTrue(rendered.htmlBody().contains("#243342"), "HTML must contain CareSync text color");
        assertTrue(rendered.htmlBody().contains("CareSync"));
        assertTrue(rendered.htmlBody().contains("Healthcare, connected."));
        assertTrue(rendered.htmlBody().contains("Dr. Sarah Smith"));
        assertTrue(rendered.textBody().contains("APT-12345678"));
    }

    // =========================================================================
    // 2. Notification Preferences Tests
    // =========================================================================

    @Test
    @DisplayName("Default preferences are opted-in, and can be customized and updated")
    void testNotificationPreferencesLifecycle() {
        NotificationPreferenceDto initial = preferenceService.getPreferences(PATIENT_USER_ID);
        assertNotNull(initial);
        assertTrue(initial.isEmailConfirmations());
        assertTrue(initial.isEmailCancellations());
        assertTrue(initial.isEmailReminders());
        assertTrue(initial.isEmailAccountAlerts());

        // Custom update
        NotificationPreferenceDto updateDto = new NotificationPreferenceDto();
        updateDto.setEmailConfirmations(true);
        updateDto.setEmailCancellations(false);
        updateDto.setEmailReminders(false);
        updateDto.setEmailAccountAlerts(true);

        preferenceService.updatePreferences(PATIENT_USER_ID, updateDto);

        NotificationPreferenceDto updated = preferenceService.getPreferences(PATIENT_USER_ID);
        assertTrue(updated.isEmailConfirmations());
        assertFalse(updated.isEmailCancellations());
        assertFalse(updated.isEmailReminders());
        assertTrue(updated.isEmailAccountAlerts());
    }

    // =========================================================================
    // 3. In-App Notification Inbox & Security Isolation Tests
    // =========================================================================

    @Test
    @DisplayName("In-app notifications support unread filter, counting, and mark-as-read")
    void testInAppNotificationInbox() {
        notificationService.sendInAppNotification(PATIENT_USER_ID, "Booking 1", "Your booking was created", "APPOINTMENT_BOOKED", "APPOINTMENT", 1L);
        notificationService.sendInAppNotification(PATIENT_USER_ID, "Booking 2", "Your booking was confirmed", "APPOINTMENT_CONFIRMED", "APPOINTMENT", 2L);

        assertEquals(2, notificationService.countUserNotifications(PATIENT_USER_ID, false));
        assertEquals(2, notificationService.getUnreadCount(PATIENT_USER_ID));

        List<NotificationDto> all = notificationService.getUserNotifications(PATIENT_USER_ID, 10, 0, false);
        assertEquals(2, all.size());

        Long firstId = all.get(0).getId();

        // Mark single as read
        boolean marked = notificationService.markAsRead(firstId, PATIENT_USER_ID);
        assertTrue(marked);
        assertEquals(1, notificationService.getUnreadCount(PATIENT_USER_ID));

        // Mark all as read
        notificationService.markAllAsRead(PATIENT_USER_ID);
        assertEquals(0, notificationService.getUnreadCount(PATIENT_USER_ID));
    }

    @Test
    @DisplayName("Security: User cannot mark another user's notification as read")
    void testSecurityIsolationForMarkAsRead() {
        Notification note = notificationService.sendInAppNotification(PATIENT_USER_ID, "Private Note", "Patient details", "SYSTEM_ALERT", null, null);
        assertNotNull(note);

        // Doctor user tries to mark patient's notification as read
        boolean tampered = notificationService.markAsRead(note.getId(), DOCTOR_USER_ID);
        assertFalse(tampered, "User should NOT be able to mark another user's notification as read");
        assertEquals(1, notificationService.getUnreadCount(PATIENT_USER_ID));
    }

    // =========================================================================
    // 4. Notification Event Dispatch & Outbox Idempotency Tests
    // =========================================================================

    @Test
    @DisplayName("notifyAppointmentBooked generates in-app notifications and outbox records with idempotency")
    void testAppointmentBookedEventAndIdempotency() {
        Patient pt = createTestPatient(PATIENT_USER_ID, "John Doe", "john@example.com");
        Doctor doc = createTestDoctor(DOCTOR_USER_ID, "Dr. Sarah Smith", "sarah@caresync.com");
        Appointment apt = createTestAppointment(10L, "APT-BOOK123", pt.getId(), doc.getId(), LocalDate.now().plusDays(2), LocalTime.of(14, 0));

        eventService.notifyAppointmentBooked(apt, pt, doc);

        // In-app notifications generated for both
        assertEquals(1, notificationService.getUnreadCount(PATIENT_USER_ID));
        assertEquals(1, notificationService.getUnreadCount(DOCTOR_USER_ID));

        // Outbox items generated
        List<NotificationDelivery> pending = deliveryDao.claimPendingDeliveries(10, 60);
        assertEquals(2, pending.size(), "Both patient and doctor email deliveries should be enqueued");

        // Idempotency check: triggering again should not duplicate delivery
        eventService.notifyAppointmentBooked(apt, pt, doc);
        assertEquals(2, deliveryDao.countAll(null), "Deliveries should not duplicate due to unique idempotency key");
    }

    @Test
    @DisplayName("User preference opt-out prevents email dispatch while preserving in-app notification")
    void testOptOutPreferenceRespected() {
        Patient pt = createTestPatient(PATIENT_USER_ID, "John Doe", "john@example.com");
        Doctor doc = createTestDoctor(DOCTOR_USER_ID, "Dr. Sarah Smith", "sarah@caresync.com");
        Appointment apt = createTestAppointment(20L, "APT-OPT123", pt.getId(), doc.getId(), LocalDate.now().plusDays(3), LocalTime.of(9, 0));

        // Disable confirmations for patient
        NotificationPreferenceDto updateDto = new NotificationPreferenceDto();
        updateDto.setEmailConfirmations(false);
        preferenceService.updatePreferences(PATIENT_USER_ID, updateDto);

        eventService.notifyAppointmentBooked(apt, pt, doc);

        // Patient still gets in-app notification
        assertEquals(1, notificationService.getUnreadCount(PATIENT_USER_ID));

        // Patient does NOT get outbox email delivery, only doctor does
        List<NotificationDelivery> outbox = deliveryDao.findAll(10, 0, null);
        assertEquals(1, outbox.size());
        assertEquals("sarah@caresync.com", outbox.get(0).getRecipientEmail());
    }

    // =========================================================================
    // 5. Dispatcher, Mock Provider & Exponential Backoff Tests
    // =========================================================================

    @Test
    @DisplayName("Dispatcher delivers pending outbox items and updates status to SENT")
    void testDispatcherSuccessfulDelivery() {
        Patient pt = createTestPatient(PATIENT_USER_ID, "John Doe", "john@example.com");
        Doctor doc = createTestDoctor(DOCTOR_USER_ID, "Dr. Sarah Smith", "sarah@caresync.com");
        Appointment apt = createTestAppointment(30L, "APT-DISP1", pt.getId(), doc.getId(), LocalDate.now().plusDays(1), LocalTime.of(11, 0));

        eventService.notifyAppointmentConfirmed(apt, pt, doc);

        int processed = dispatcher.dispatchPendingDeliveries(10);
        assertEquals(1, processed);

        List<MockNotificationProvider.SentMessage> sent = mockEmailProvider.getSentMessages();
        assertEquals(1, sent.size());
        assertEquals("john@example.com", sent.get(0).recipientAddress());
        assertTrue(sent.get(0).subject().contains("APT-DISP1"));

        // Check outbox item is marked SENT
        List<NotificationDelivery> items = deliveryDao.findAll(10, 0, NotificationDeliveryStatus.SENT);
        assertEquals(1, items.size());
        assertNotNull(items.get(0).getSentAt());
    }

    @Test
    @DisplayName("Dispatcher handles provider failures with retry schedule and eventual FAILED state")
    void testDispatcherRetryAndBackoff() {
        Patient pt = createTestPatient(PATIENT_USER_ID, "John Doe", "john@example.com");
        Doctor doc = createTestDoctor(DOCTOR_USER_ID, "Dr. Sarah Smith", "sarah@caresync.com");
        Appointment apt = createTestAppointment(40L, "APT-FAIL1", pt.getId(), doc.getId(), LocalDate.now().plusDays(1), LocalTime.of(15, 0));

        eventService.notifyAppointmentConfirmed(apt, pt, doc);

        // Configure mock provider to fail
        mockEmailProvider.setShouldFail(true);
        mockEmailProvider.setFailureMessage("SMTP Connection Timeout");

        // Attempt 1
        dispatcher.dispatchPendingDeliveries(10);
        NotificationDelivery item = deliveryDao.findAll(10, 0, null).get(0);
        assertEquals(NotificationDeliveryStatus.FAILED, item.getStatus());
        assertEquals(1, item.getAttemptCount());
        assertEquals("SMTP Connection Timeout", item.getLastError());

        // Admin manual retry
        boolean retried = dispatcher.retryDelivery(item.getId());
        assertTrue(retried);
        NotificationDelivery resetItem = deliveryDao.findById(item.getId()).orElseThrow();
        assertEquals(0, resetItem.getAttemptCount());
    }

    // =========================================================================
    // 6. Scheduler & Reminder Scan Tests
    // =========================================================================

    @Test
    @DisplayName("Scheduler scans upcoming appointments within lead hours and enqueues reminders")
    void testSchedulerReminderScan() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Patient pt = createTestPatient(PATIENT_USER_ID, "John Doe", "john@example.com");
        Doctor doc = createTestDoctor(DOCTOR_USER_ID, "Dr. Sarah Smith", "sarah@caresync.com");
        Appointment apt = createTestAppointment(50L, "APT-REM123", pt.getId(), doc.getId(), tomorrow, LocalTime.of(16, 0));
        apt.setStatus(AppointmentStatus.CONFIRMED);
        apt.setPatient(pt);
        apt.setDoctor(doc);

        InMemoryAppointmentDao appointmentDao = new InMemoryAppointmentDao();
        appointmentDao.save(apt);

        NotificationScheduler scheduler = new NotificationSchedulerImpl(
                dispatcher,
                eventService,
                appointmentDao,
                null,
                null,
                null
        );

        int scanned = scheduler.triggerReminderScanNow();
        assertEquals(1, scanned);

        // In-app reminder created
        assertEquals(1, notificationService.getUnreadCount(PATIENT_USER_ID));
        NotificationDto note = notificationService.getUserNotifications(PATIENT_USER_ID, 1, 0, true).get(0);
        assertEquals(NotificationType.APPOINTMENT_REMINDER, note.getNotificationType());

        // Outbox email queued
        List<NotificationDelivery> outbox = deliveryDao.findAll(10, 0, null);
        assertEquals(1, outbox.size());
        assertEquals(NotificationType.APPOINTMENT_REMINDER, outbox.get(0).getEventType());

        // Second scan produces 0 duplicates due to idempotency
        scheduler.triggerReminderScanNow();
        assertEquals(1, deliveryDao.countAll(null));
    }

    // =========================================================================
    // Helpers & In-Memory Fakes
    // =========================================================================

    private Patient createTestPatient(Long id, String name, String email) {
        Patient p = new Patient(id);
        p.setFirstName(name);
        p.setEmail(email);
        return p;
    }

    private Doctor createTestDoctor(Long id, String name, String email) {
        Doctor d = new Doctor(id);
        d.setFirstName(name);
        d.setEmail(email);
        return d;
    }

    private Appointment createTestAppointment(Long id, String ref, Long ptId, Long docId, LocalDate date, LocalTime time) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setReferenceNumber(ref);
        a.setPatientId(ptId);
        a.setDoctorId(docId);
        a.setAppointmentDate(date);
        a.setStartTime(time);
        a.setEndTime(time.plusMinutes(30));
        a.setStatus(AppointmentStatus.PENDING);
        return a;
    }

    private static class InMemoryNotificationDao implements NotificationDao {
        private final Map<Long, Notification> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override
        public Optional<Notification> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Notification> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Notification> findByUserId(Long userId) {
            return store.values().stream().filter(n -> userId.equals(n.getUserId())).collect(Collectors.toList());
        }

        @Override
        public List<Notification> findUnreadByUserId(Long userId) {
            return store.values().stream().filter(n -> userId.equals(n.getUserId()) && !n.isRead()).collect(Collectors.toList());
        }

        @Override
        public long countUnreadByUserId(Long userId) {
            return findUnreadByUserId(userId).size();
        }

        @Override
        public boolean markAsRead(Long notificationId) {
            Notification n = store.get(notificationId);
            if (n != null) {
                n.setRead(true);
                return true;
            }
            return false;
        }

        @Override
        public boolean markAllAsRead(Long userId) {
            store.values().stream().filter(n -> userId.equals(n.getUserId())).forEach(n -> n.setRead(true));
            return true;
        }

        @Override
        public Notification save(Notification n, Connection conn) {
            return save(n);
        }

        @Override
        public Notification save(Notification n) {
            if (n.getId() == null) {
                n.setId(idGen.getAndIncrement());
                n.setCreatedAt(Instant.now());
            }
            store.put(n.getId(), n);
            return n;
        }

        @Override
        public boolean update(Notification entity) {
            store.put(entity.getId(), entity);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }

        @Override
        public List<Notification> findPaginatedByUserId(Long userId, int offset, int limit) {
            return findByUserId(userId).stream().skip(offset).limit(limit).collect(Collectors.toList());
        }

        @Override
        public long countByUserId(Long userId) {
            return findByUserId(userId).size();
        }

        @Override
        public List<Notification> findPaginatedAll(String query, String type, int offset, int limit) {
            return store.values().stream().skip(offset).limit(limit).collect(Collectors.toList());
        }

        @Override
        public long countFilteredAll(String query, String type) {
            return store.size();
        }
    }

    private static class InMemoryNotificationPreferenceDao implements NotificationPreferenceDao {
        private final Map<Long, NotificationPreference> store = new HashMap<>();

        @Override
        public Optional<NotificationPreference> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<NotificationPreference> findByUserId(Long userId) {
            return store.values().stream().filter(p -> userId.equals(p.getUserId())).findFirst();
        }

        @Override
        public List<NotificationPreference> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public NotificationPreference save(NotificationPreference entity) {
            return upsert(entity);
        }

        @Override
        public NotificationPreference upsert(NotificationPreference pref) {
            if (pref.getId() == null) {
                pref.setId(pref.getUserId());
            }
            store.put(pref.getUserId(), pref);
            return pref;
        }

        @Override
        public boolean update(NotificationPreference entity) {
            upsert(entity);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }
    }

    private static class InMemoryNotificationDeliveryDao implements NotificationDeliveryDao {
        private final Map<Long, NotificationDelivery> store = new HashMap<>();
        private final AtomicLong idGen = new AtomicLong(1);

        @Override
        public Optional<NotificationDelivery> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<NotificationDelivery> findByIdempotencyKey(String idempotencyKey) {
            return store.values().stream().filter(d -> idempotencyKey.equals(d.getIdempotencyKey())).findFirst();
        }

        @Override
        public List<NotificationDelivery> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<NotificationDelivery> findAll(int limit, int offset, NotificationDeliveryStatus statusFilter) {
            return store.values().stream()
                    .filter(d -> statusFilter == null || d.getStatus() == statusFilter)
                    .skip(offset)
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        @Override
        public long countAll(NotificationDeliveryStatus statusFilter) {
            return store.values().stream().filter(d -> statusFilter == null || d.getStatus() == statusFilter).count();
        }

        @Override
        public NotificationDelivery save(NotificationDelivery delivery) {
            if (delivery.getIdempotencyKey() != null) {
                var existing = findByIdempotencyKey(delivery.getIdempotencyKey());
                if (existing.isPresent()) {
                    return existing.get();
                }
            }
            if (delivery.getId() == null) {
                delivery.setId(idGen.getAndIncrement());
                delivery.setCreatedAt(Instant.now());
            }
            store.put(delivery.getId(), delivery);
            return delivery;
        }

        @Override
        public List<NotificationDelivery> claimPendingDeliveries(int limit, int leaseSeconds) {
            List<NotificationDelivery> claimed = store.values().stream()
                    .filter(d -> d.getStatus() == NotificationDeliveryStatus.PENDING || d.getStatus() == NotificationDeliveryStatus.FAILED)
                    .limit(limit)
                    .collect(Collectors.toList());
            for (NotificationDelivery d : claimed) {
                d.setStatus(NotificationDeliveryStatus.PROCESSING);
                d.setAttemptCount(d.getAttemptCount() + 1);
            }
            return claimed;
        }

        @Override
        public void updateStatus(Long outboxId, NotificationDeliveryStatus status, String lastError, Instant sentAt) {
            NotificationDelivery d = store.get(outboxId);
            if (d != null) {
                d.setStatus(status);
                d.setLastError(lastError);
                d.setSentAt(sentAt);
            }
        }

        @Override
        public void scheduleRetry(Long outboxId, int attemptCount, Instant nextRetryAt, String lastError) {
            NotificationDelivery d = store.get(outboxId);
            if (d != null) {
                d.setStatus(NotificationDeliveryStatus.FAILED);
                d.setAttemptCount(attemptCount);
                d.setNextRetryAt(nextRetryAt);
                d.setLastError(lastError);
            }
        }

        @Override
        public int recoverTimedOutLeases() {
            return 0;
        }

        @Override
        public boolean update(NotificationDelivery entity) {
            store.put(entity.getId(), entity);
            return true;
        }

        @Override
        public boolean deleteById(Long id) {
            return store.remove(id) != null;
        }
    }

    private static class InMemoryAppointmentDao implements AppointmentDao {
        private final List<Appointment> list = new ArrayList<>();

        @Override
        public List<Appointment> findByDate(LocalDate date) {
            return list.stream().filter(a -> date.equals(a.getAppointmentDate())).collect(Collectors.toList());
        }

        @Override
        public Optional<Appointment> findById(Long id) {
            return list.stream().filter(a -> id.equals(a.getId())).findFirst();
        }

        @Override
        public List<Appointment> findAll() { return list; }

        @Override
        public Appointment save(Appointment a) {
            list.add(a);
            return a;
        }

        @Override
        public boolean update(Appointment entity) { return true; }

        @Override
        public boolean deleteById(Long id) { return true; }

        @Override
        public Optional<Appointment> findByReference(String reference) { return Optional.empty(); }

        @Override
        public List<Appointment> findByDoctorId(Long doctorId) { return List.of(); }

        @Override
        public List<Appointment> findByDoctorAndDate(Long doctorId, LocalDate date) { return List.of(); }

        @Override
        public List<Appointment> findByPatientId(Long patientId) { return List.of(); }

        @Override
        public boolean updateStatus(Long appointmentId, AppointmentStatus status, String cancellationReason, Long cancelledByUserId) { return true; }
    }
}
