-- V13: Request extensions — provider can request more days + updated price

CREATE TABLE request_extensions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    additional_days INT NOT NULL,
    original_price DECIMAL(10, 2) NULL,
    updated_price DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    responded_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_extension_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_extension_provider FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_extension_request ON request_extensions(service_request_id);
CREATE INDEX idx_extension_provider ON request_extensions(provider_id);
CREATE INDEX idx_extension_status ON request_extensions(status);
