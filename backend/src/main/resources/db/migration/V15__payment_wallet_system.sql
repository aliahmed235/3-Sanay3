
CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(500),
    service_request_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wt_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
    CONSTRAINT fk_wt_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    INDEX idx_wallet_id (wallet_id),
    INDEX idx_transaction_created_at (created_at)
);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    platform_fee DECIMAL(12, 2) NOT NULL,
    provider_earning DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id),
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_payment_provider FOREIGN KEY (provider_id) REFERENCES users(id),
    INDEX idx_payment_request_id (service_request_id),
    INDEX idx_payment_customer_id (customer_id),
    INDEX idx_payment_provider_id (provider_id),
    INDEX idx_payment_status (status)
);

ALTER TABLE users ADD COLUMN banned BOOLEAN NOT NULL DEFAULT FALSE;
