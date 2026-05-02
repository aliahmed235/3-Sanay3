UPDATE service_provider_profiles
SET service_type = 'CARPENTER'
WHERE service_type = 'GAS';

UPDATE service_requests
SET service_type = 'CARPENTER'
WHERE service_type = 'GAS';
