-- V6: Restructure chat_rooms to be per customer-provider pair (not per request)
-- This allows the same chat room to be reused across multiple requests

-- Step 1: Add chat_room_id column to service_requests
ALTER TABLE service_requests ADD COLUMN chat_room_id BIGINT;
ALTER TABLE service_requests ADD CONSTRAINT fk_service_request_chat_room
    FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE SET NULL;

-- Step 2: Migrate existing data - copy chat room links from chat_rooms.request_id to service_requests.chat_room_id
UPDATE service_requests sr
    INNER JOIN chat_rooms cr ON cr.request_id = sr.id
SET sr.chat_room_id = cr.id;

-- Step 3: Drop the unique constraint and foreign key on chat_rooms.request_id
ALTER TABLE chat_rooms DROP FOREIGN KEY chat_rooms_ibfk_1;
ALTER TABLE chat_rooms DROP INDEX request_id;

-- Step 4: Drop the request_id column from chat_rooms (no longer needed)
ALTER TABLE chat_rooms DROP COLUMN request_id;

-- Step 5: Add unique constraint on customer_id + provider_id (one chat room per pair)
ALTER TABLE chat_rooms ADD CONSTRAINT uk_customer_provider UNIQUE (customer_id, provider_id);

-- Step 6: Add index on service_requests.chat_room_id
CREATE INDEX idx_chat_room_id ON service_requests(chat_room_id);
