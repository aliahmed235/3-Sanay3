
CREATE TABLE users (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       phone VARCHAR(20),
                       password VARCHAR(255) NOT NULL,
                       address VARCHAR(255),
                       latitude DECIMAL(10, 8),
                       longitude DECIMAL(11, 8),
                       profile_image VARCHAR(255),
                       rating DECIMAL(3, 2) DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE service_providers (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   name VARCHAR(100) NOT NULL,
                                   email VARCHAR(100) UNIQUE NOT NULL,
                                   phone VARCHAR(20),
                                   password VARCHAR(255) NOT NULL,
                                   address VARCHAR(255),
                                   latitude DECIMAL(10, 8),
                                   longitude DECIMAL(11, 8),
                                   profile_image VARCHAR(255),
                                   specialization VARCHAR(100) NOT NULL,
                                   hourly_rate DECIMAL(8, 2),
                                   bio TEXT,
                                   rating DECIMAL(3, 2) DEFAULT 0,
                                   is_verified BOOLEAN DEFAULT FALSE,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE admin (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_service_providers_email ON service_providers(email);
CREATE INDEX idx_service_providers_specialization ON service_providers(specialization);
CREATE INDEX idx_admin_email ON admin(email);