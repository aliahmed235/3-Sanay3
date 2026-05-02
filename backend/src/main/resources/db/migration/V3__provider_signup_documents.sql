ALTER TABLE service_provider_profiles
    ADD COLUMN national_id VARCHAR(20) NULL AFTER service_type,
    ADD COLUMN has_criminal_record BOOLEAN NOT NULL DEFAULT false AFTER rejection_reason;

CREATE UNIQUE INDEX uk_service_provider_profiles_national_id
    ON service_provider_profiles(national_id);
