-- V9: Add scheduling, work summary, and work photos

-- Scheduled requests: customer can pick a future date/time
ALTER TABLE service_requests ADD COLUMN scheduled_at DATETIME NULL;

-- Work summary: provider describes what they did after completing
ALTER TABLE service_requests ADD COLUMN work_summary TEXT NULL;

-- Work photos: provider uploads photos of completed work
CREATE TABLE work_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_photo_request FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_photo_request ON work_photos(request_id);
CREATE INDEX idx_scheduled_at ON service_requests(scheduled_at);
