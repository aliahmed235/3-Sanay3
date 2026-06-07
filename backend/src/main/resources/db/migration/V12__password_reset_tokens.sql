-- V12: Password reset tokens for forgot-password flow

CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    code VARCHAR(6) NOT NULL,
    reset_token VARCHAR(255) NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INT NOT NULL DEFAULT 0,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_reset_token_user ON password_reset_tokens(user_id);
CREATE INDEX idx_reset_token_code ON password_reset_tokens(user_id, code);
CREATE INDEX idx_reset_token_token ON password_reset_tokens(reset_token);
CREATE INDEX idx_reset_token_expires ON password_reset_tokens(expires_at);
