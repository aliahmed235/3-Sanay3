-- V10: AI Nudge infrastructure - behavior events, FCM tokens, notification logs

-- Track user behavior events for AI-driven nudges
CREATE TABLE user_behavior_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    service_type VARCHAR(50) NULL,
    service_request_id BIGINT NULL,
    session_id VARCHAR(100) NULL,
    client_timezone VARCHAR(50) NULL,
    occurred_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_behavior_event_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_behavior_event_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE SET NULL
);

CREATE INDEX idx_behavior_event_user ON user_behavior_events(user_id);
CREATE INDEX idx_behavior_event_type ON user_behavior_events(event_type);
CREATE INDEX idx_behavior_event_occurred ON user_behavior_events(occurred_at);
CREATE INDEX idx_behavior_event_user_type ON user_behavior_events(user_id, event_type);

-- Store device FCM tokens for push notifications
CREATE TABLE user_fcm_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fcm_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_fcm_token UNIQUE (token)
);

CREATE INDEX idx_fcm_token_user ON user_fcm_tokens(user_id);
CREATE INDEX idx_fcm_token_user_active ON user_fcm_tokens(user_id, active);

-- Log all notifications sent (audit trail + anti-spam)
CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    rule_name VARCHAR(100) NULL,
    service_type VARCHAR(50) NULL,
    title VARCHAR(255) NOT NULL,
    body VARCHAR(500) NOT NULL,
    deep_link VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    fcm_message_id VARCHAR(255) NULL,
    failure_reason VARCHAR(500) NULL,
    ai_explanation VARCHAR(500) NULL,
    sent_at DATETIME NULL,
    opened_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_log_user ON notification_logs(user_id);
CREATE INDEX idx_notification_log_type ON notification_logs(notification_type);
CREATE INDEX idx_notification_log_user_type ON notification_logs(user_id, notification_type);
CREATE INDEX idx_notification_log_status ON notification_logs(status);
