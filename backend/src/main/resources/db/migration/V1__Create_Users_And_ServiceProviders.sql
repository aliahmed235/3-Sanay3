CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     name ENUM('USER', 'SERVICE_PROVIDER', 'ADMIN') NOT NULL UNIQUE,
                                     description VARCHAR(255),
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO roles (name, description) VALUES
                                                 ('USER', 'Customer can make service requests'),
                                                 ('SERVICE_PROVIDER', 'Service provider can accept requests and earn'),
                                                 ('ADMIN', 'Admin can verify providers and manage platform');

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(100) NOT NULL,
                                     email VARCHAR(100) UNIQUE NOT NULL,
                                     phone VARCHAR(20) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     address VARCHAR(255),
                                     latitude DECIMAL(10, 8),
                                     longitude DECIMAL(11, 8),
                                     profile_image VARCHAR(255),
                                     is_active BOOLEAN DEFAULT TRUE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     INDEX idx_email (email),
                                     INDEX idx_phone (phone),
                                     INDEX idx_is_active (is_active)
);

CREATE TABLE IF NOT EXISTS user_roles (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                          FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                          UNIQUE KEY unique_user_role (user_id, role_id),
                                          INDEX idx_user_id (user_id),
                                          INDEX idx_role_id (role_id)
);

CREATE TABLE IF NOT EXISTS customer_profiles (
                                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                 user_id BIGINT NOT NULL UNIQUE,
                                                 address VARCHAR(255),
                                                 latitude DECIMAL(10, 8),
                                                 longitude DECIMAL(11, 8),
                                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                                 INDEX idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS service_provider_profiles (
                                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                         user_id BIGINT NOT NULL UNIQUE,
                                                         service_type ENUM('GAS', 'WATER', 'ELECTRICITY') NOT NULL,
                                                         hourly_rate DECIMAL(8, 2),
                                                         bio TEXT,
                                                         address VARCHAR(255),
                                                         latitude DECIMAL(10, 8),
                                                         longitude DECIMAL(11, 8),
                                                         is_verified BOOLEAN DEFAULT FALSE,
                                                         verification_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
                                                         verified_by_admin_id BIGINT,
                                                         verification_date TIMESTAMP NULL,
                                                         rejection_reason TEXT,
                                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                                         FOREIGN KEY (verified_by_admin_id) REFERENCES users(id) ON DELETE SET NULL,
                                                         INDEX idx_service_type (service_type),
                                                         INDEX idx_verification_status (verification_status),
                                                         INDEX idx_is_verified (is_verified)
);

CREATE TABLE IF NOT EXISTS provider_documents (
                                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                  service_provider_id BIGINT NOT NULL,
                                                  document_type ENUM('NATIONAL_ID', 'CRIMINAL_HISTORY') NOT NULL,
                                                  document_name VARCHAR(255) NOT NULL,
                                                  document_url VARCHAR(500) NOT NULL,
                                                  is_verified BOOLEAN DEFAULT FALSE,
                                                  uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  FOREIGN KEY (service_provider_id) REFERENCES service_provider_profiles(id) ON DELETE CASCADE,
                                                  INDEX idx_service_provider_id (service_provider_id),
                                                  INDEX idx_document_type (document_type),
                                                  INDEX idx_is_verified (is_verified)
);