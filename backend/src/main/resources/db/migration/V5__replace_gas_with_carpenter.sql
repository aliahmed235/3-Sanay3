ALTER TABLE service_provider_profiles
    MODIFY COLUMN service_type VARCHAR(50) NOT NULL;

ALTER TABLE service_requests
    MODIFY COLUMN service_type VARCHAR(50) NOT NULL;

UPDATE service_provider_profiles
SET service_type = 'CARPENTER'
WHERE service_type = 'GAS';

UPDATE service_requests
SET service_type = 'CARPENTER'
WHERE service_type = 'GAS';
