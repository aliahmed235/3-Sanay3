-- V6: Restructure chat_rooms to be per customer-provider pair (not per request)
-- Idempotent: safe to re-run if a previous attempt partially applied

-- flyway:delimiter=//

CREATE PROCEDURE migrate_chat_rooms_v6()
BEGIN
    -- Step 1: Add chat_room_id column to service_requests if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'service_requests' AND COLUMN_NAME = 'chat_room_id'
    ) THEN
        ALTER TABLE service_requests ADD COLUMN chat_room_id BIGINT;
    END IF;

    -- Step 2: Add FK constraint if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'service_requests' AND CONSTRAINT_NAME = 'fk_service_request_chat_room'
    ) THEN
        ALTER TABLE service_requests ADD CONSTRAINT fk_service_request_chat_room
            FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE SET NULL;
    END IF;

    -- Step 3: Migrate data + drop request_id (only if request_id still exists)
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_rooms' AND COLUMN_NAME = 'request_id'
    ) THEN
        -- Copy chat room links to service_requests
        UPDATE service_requests sr
            INNER JOIN chat_rooms cr ON cr.request_id = sr.id
        SET sr.chat_room_id = cr.id
        WHERE sr.chat_room_id IS NULL;

        -- Drop FK on request_id (find actual name dynamically)
        BEGIN
            DECLARE fk_name VARCHAR(255);
            SELECT CONSTRAINT_NAME INTO fk_name
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'chat_rooms'
              AND COLUMN_NAME = 'request_id'
              AND REFERENCED_TABLE_NAME IS NOT NULL
            LIMIT 1;

            IF fk_name IS NOT NULL THEN
                SET @drop_fk = CONCAT('ALTER TABLE chat_rooms DROP FOREIGN KEY `', fk_name, '`');
                PREPARE stmt FROM @drop_fk;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;
            END IF;
        END;

        -- Drop index on request_id if exists
        IF EXISTS (
            SELECT 1 FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_rooms' AND INDEX_NAME = 'request_id'
        ) THEN
            DROP INDEX request_id ON chat_rooms;
        END IF;

        -- Drop the request_id column
        ALTER TABLE chat_rooms DROP COLUMN request_id;
    END IF;

    -- Step 4: Add unique constraint if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'chat_rooms' AND CONSTRAINT_NAME = 'uk_customer_provider'
    ) THEN
        ALTER TABLE chat_rooms ADD CONSTRAINT uk_customer_provider UNIQUE (customer_id, provider_id);
    END IF;

    -- Step 5: Add index if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'service_requests' AND INDEX_NAME = 'idx_chat_room_id'
    ) THEN
        CREATE INDEX idx_chat_room_id ON service_requests(chat_room_id);
    END IF;
END//

CALL migrate_chat_rooms_v6()//

DROP PROCEDURE IF EXISTS migrate_chat_rooms_v6//
