-- V8: Add photo_url column to service_requests
ALTER TABLE service_requests ADD COLUMN photo_url VARCHAR(500) NULL;
