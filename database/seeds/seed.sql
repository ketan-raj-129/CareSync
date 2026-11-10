-- ==============================================================================
-- CareSync — Relational Database Initial Seed Data
-- ==============================================================================

-- 1. Medical Specialties
INSERT INTO specialties (name, description, icon_name, is_active) VALUES
('Cardiology', 'Diagnosis and treatment of congenital heart defects, coronary artery disease, heart failure, and valvular heart disease.', 'heart-pulse', TRUE),
('Dermatology', 'Management of disorders of the skin, hair, nails, and adjacent mucous membranes.', 'shield', TRUE),
('Pediatrics', 'Comprehensive medical care for infants, children, and adolescents.', 'baby', TRUE),
('Neurology', 'Diagnosis and management of disorders of the brain, spinal cord, and peripheral nerves.', 'brain', TRUE),
('Orthopedics', 'Treatment of conditions involving the musculoskeletal system including bones, joints, and ligaments.', 'bone', TRUE),
('Ophthalmology', 'Comprehensive vision examination, eye disease diagnosis, and surgical eye care.', 'eye', TRUE),
('Psychiatry', 'Assessment, diagnosis, and treatment of mental health, emotional, and behavioral disorders.', 'smile', TRUE),
('General Practice', 'Primary, comprehensive, and continuing whole-person healthcare for individuals and families.', 'stethoscope', TRUE);

-- 2. System Operational Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('clinic.name', 'CareSync Medical Center', 'Official clinic display name'),
('clinic.advance_booking_days', '60', 'Maximum days in advance a patient may book an appointment'),
('clinic.cancellation_window_hours', '2', 'Minimum notice in hours required for online patient cancellation'),
('clinic.default_slot_duration_minutes', '30', 'Standard duration in minutes for consultation time slots'),
('clinic.contact_email', 'support@caresync.com', 'Primary clinic patient support email address'),
('clinic.contact_phone', '+1 (800) 555-CARE', 'Clinic emergency and reception contact number');

-- 3. Standard Clinic Holidays (doctor_id = NULL denotes clinic-wide closure)
INSERT INTO doctor_availability_exceptions (doctor_id, exception_date, is_full_day, exception_type, reason) VALUES
(NULL, '2026-12-25', TRUE, 'HOLIDAY', 'Christmas Day Observance'),
(NULL, '2027-01-01', TRUE, 'HOLIDAY', 'New Year''s Day Observance');

