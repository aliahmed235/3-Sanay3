-- V1__Create_Users_And_ServiceProviders.sql
-- Create all base tables

-- ========================================
-- Roles Table
-- ========================================
CREATE TABLE roles (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ========================================
-- Users Table
-- ========================================
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       phone VARCHAR(20) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       address VARCHAR(255),
                       latitude DECIMAL(10,8),
                       longitude DECIMAL(10,8),
                       profile_image VARCHAR(255),
                       is_active BOOLEAN DEFAULT true NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

-- ========================================
-- User Roles Table
-- ========================================
CREATE TABLE user_roles (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            UNIQUE KEY uk_user_role (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- ========================================
-- Customer Profiles Table
-- ========================================
CREATE TABLE customer_profiles (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL UNIQUE,
                                   address VARCHAR(255),
                                   latitude DECIMAL(10,8),
                                   longitude DECIMAL(10,8),
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========================================
-- Service Provider Profiles Table
-- ========================================
CREATE TABLE service_provider_profiles (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           user_id BIGINT NOT NULL UNIQUE,
                                           service_type VARCHAR(50) NOT NULL,
                                           hourly_rate DECIMAL(8,2),
                                           bio TEXT,
                                           address VARCHAR(255),
                                           latitude DECIMAL(10,8),
                                           longitude DECIMAL(10,8),
                                           is_verified BOOLEAN NOT NULL DEFAULT false,
                                           verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                           verified_by_admin_id BIGINT,
                                           verification_date TIMESTAMP,
                                           rejection_reason TEXT,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

                                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                           FOREIGN KEY (verified_by_admin_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ========================================
-- Provider Documents Table
-- ========================================
CREATE TABLE provider_documents (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    service_provider_id BIGINT NOT NULL,
                                    document_type VARCHAR(50) NOT NULL,
                                    document_name VARCHAR(255) NOT NULL,
                                    document_url VARCHAR(500) NOT NULL,
                                    is_verified BOOLEAN NOT NULL DEFAULT false,
                                    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                                    FOREIGN KEY (service_provider_id) REFERENCES service_provider_profiles(id) ON DELETE CASCADE
);

-- ========================================
-- Service Requests Table
-- ========================================
CREATE TABLE service_requests (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  customer_id BIGINT NOT NULL,
                                  service_type VARCHAR(50) NOT NULL,
                                  title VARCHAR(255) NOT NULL,
                                  description TEXT,
                                  address VARCHAR(255),
                                  latitude DECIMAL(10,8) NOT NULL,
                                  longitude DECIMAL(10,8) NOT NULL,
                                  status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                                  accepted_provider_id BIGINT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
                                  expires_at TIMESTAMP NOT NULL,
                                  accepted_at TIMESTAMP,
                                  started_at TIMESTAMP,
                                  completed_at TIMESTAMP,

                                  FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE RESTRICT,
                                  FOREIGN KEY (accepted_provider_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ========================================
-- Service Offers Table
-- ========================================
CREATE TABLE service_offers (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                request_id BIGINT NOT NULL,
                                provider_id BIGINT NOT NULL,
                                offered_price DECIMAL(10,2) NOT NULL,
                                estimated_time_minutes INT NOT NULL,
                                description TEXT,
                                status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                responded_at TIMESTAMP,

                                UNIQUE KEY uk_request_provider_unique (request_id, provider_id),
                                FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
                                FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- ========================================
-- Chat Rooms Table
-- ========================================
CREATE TABLE chat_rooms (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            request_id BIGINT NOT NULL UNIQUE,
                            customer_id BIGINT NOT NULL,
                            provider_id BIGINT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,

                            FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
                            FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE RESTRICT,
                            FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- ========================================
-- Chat Messages Table
-- ========================================
CREATE TABLE chat_messages (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               chat_room_id BIGINT NOT NULL,
                               sender_id BIGINT NOT NULL,
                               message TEXT,
                               message_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
                               latitude DECIMAL(10,8),
                               longitude DECIMAL(11,8),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                               FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
                               FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- ========================================
-- Ratings Table
-- ========================================
CREATE TABLE ratings (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         request_id BIGINT NOT NULL UNIQUE,
                         customer_id BIGINT NOT NULL,
                         provider_id BIGINT NOT NULL,
                         rating_value INT NOT NULL,
                         review TEXT,
                         cancellation_penalty  DOUBLE NOT NULL DEFAULT 0.0,
                         late_arrival_penalty  DOUBLE NOT NULL DEFAULT 0.0,
                         minutes_late INT,
                         incomplete_service_penalty DOUBLE NOT NULL DEFAULT 0.0,
                         incomplete_service_reason TEXT,
                         total_penalty DOUBLE,
                         final_rating DOUBLE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

                         CONSTRAINT chk_rating_value CHECK (rating_value BETWEEN 1 AND 5),
                         CONSTRAINT chk_final_rating CHECK (final_rating IS NULL OR (final_rating BETWEEN 1 AND 5)),
                         FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE CASCADE,
                         FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE RESTRICT,
                         FOREIGN KEY (provider_id) REFERENCES users(id) ON DELETE RESTRICT
);
