CREATE TABLE support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_id BIGINT NULL,
    category VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    admin_note TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,

    CONSTRAINT fk_support_ticket_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_support_ticket_request FOREIGN KEY (request_id) REFERENCES service_requests(id),

    INDEX idx_support_user_id (user_id),
    INDEX idx_support_status (status),
    INDEX idx_support_category (category),
    INDEX idx_support_created_at (created_at)
);
