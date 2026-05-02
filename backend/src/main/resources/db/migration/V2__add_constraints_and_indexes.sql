-- -- V2__add_constraints_and_indexes.sql
-- -- Add all indexes for performance
--
-- -- ========================================
-- -- User Indexes
-- -- ========================================
-- CREATE INDEX idx_users_email ON users(email);
-- CREATE INDEX idx_users_phone ON users(phone);
-- CREATE INDEX idx_users_is_active ON users(is_active);
--
-- -- ========================================
-- -- Service Request Indexes
-- -- ========================================
-- CREATE INDEX idx_service_requests_status ON service_requests(status);
-- CREATE INDEX idx_service_requests_service_type ON service_requests(service_type);
-- CREATE INDEX idx_service_requests_created_at ON service_requests(created_at);
-- CREATE INDEX idx_service_requests_customer_status ON service_requests(customer_id, status);
-- CREATE INDEX idx_service_requests_provider_status ON service_requests(accepted_provider_id, status);
-- CREATE INDEX idx_service_requests_expires_at ON service_requests(expires_at);
-- CREATE INDEX idx_service_requests_location ON service_requests(latitude, longitude);
--
-- -- ========================================
-- -- Service Offer Indexes
-- -- ========================================
-- CREATE INDEX idx_service_offers_status ON service_offers(status);
-- CREATE INDEX idx_service_offers_created_at ON service_offers(created_at);
-- CREATE INDEX idx_service_offers_provider_status ON service_offers(provider_id, status);
-- CREATE INDEX idx_service_offers_request_id ON service_offers(request_id);
--
-- -- ========================================
-- -- Chat Room Indexes
-- -- ========================================
-- CREATE INDEX idx_chat_rooms_customer_id ON chat_rooms(customer_id);
-- CREATE INDEX idx_chat_rooms_provider_id ON chat_rooms(provider_id);
-- CREATE INDEX idx_chat_rooms_created_at ON chat_rooms(created_at);
--
-- -- ========================================
-- -- Chat Message Indexes
-- -- ========================================
-- CREATE INDEX idx_chat_messages_type ON chat_messages(message_type);
-- CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
-- CREATE INDEX idx_chat_messages_room_created ON chat_messages(chat_room_id, created_at);
-- CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
--
-- -- ========================================
-- -- Rating Indexes
-- -- ========================================
-- CREATE INDEX idx_ratings_final_rating ON ratings(final_rating);
-- CREATE INDEX idx_ratings_created_at ON ratings(created_at);
-- CREATE INDEX idx_ratings_provider_id ON ratings(provider_id);
-- CREATE INDEX idx_ratings_customer_id ON ratings(customer_id);
--
-- -- ========================================
-- -- Provider Document Indexes
-- -- ========================================
-- CREATE INDEX idx_provider_documents_service_provider_id ON provider_documents(service_provider_id);
-- CREATE INDEX idx_provider_documents_document_type ON provider_documents(document_type);
--
-- -- ========================================
-- -- Customer Profile Indexes
-- -- ========================================
-- CREATE INDEX idx_customer_profiles_user_id ON customer_profiles(user_id);
--
-- -- ========================================
-- -- Service Provider Profile Indexes
-- -- ========================================
-- CREATE INDEX idx_service_provider_profiles_user_id ON service_provider_profiles(user_id);
-- CREATE INDEX idx_service_provider_profiles_service_type ON service_provider_profiles(service_type);
-- CREATE INDEX idx_service_provider_profiles_verification_status ON service_provider_profiles(verification_status);
-- CREATE INDEX idx_service_provider_profiles_is_verified ON service_provider_profiles(is_verified);
--
-- -- ========================================
-- -- User Roles Indexes
-- -- ========================================
-- CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
-- CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
-- V2__add_constraints_and_indexes.sql
-- Add all indexes for performance

-- ========================================
-- User Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- ========================================
-- Service Request Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_service_requests_service_type ON service_requests(service_type);
CREATE INDEX IF NOT EXISTS idx_service_requests_created_at ON service_requests(created_at);
CREATE INDEX IF NOT EXISTS idx_service_requests_customer_status ON service_requests(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_service_requests_provider_status ON service_requests(accepted_provider_id, status);
CREATE INDEX IF NOT EXISTS idx_service_requests_expires_at ON service_requests(expires_at);
CREATE INDEX IF NOT EXISTS idx_service_requests_location ON service_requests(latitude, longitude);

-- ========================================
-- Service Offer Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_service_offers_status ON service_offers(status);
CREATE INDEX IF NOT EXISTS idx_service_offers_created_at ON service_offers(created_at);
CREATE INDEX IF NOT EXISTS idx_service_offers_provider_status ON service_offers(provider_id, status);
CREATE INDEX IF NOT EXISTS idx_service_offers_request_id ON service_offers(request_id);

-- ========================================
-- Chat Room Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_chat_rooms_customer_id ON chat_rooms(customer_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_provider_id ON chat_rooms(provider_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_created_at ON chat_rooms(created_at);

-- ========================================
-- Chat Message Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_chat_messages_type ON chat_messages(message_type);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_messages_room_created ON chat_messages(chat_room_id, created_at);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages(sender_id);

-- ========================================
-- Rating Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_ratings_final_rating ON ratings(final_rating);
CREATE INDEX IF NOT EXISTS idx_ratings_created_at ON ratings(created_at);
CREATE INDEX IF NOT EXISTS idx_ratings_provider_id ON ratings(provider_id);
CREATE INDEX IF NOT EXISTS idx_ratings_customer_id ON ratings(customer_id);

-- ========================================
-- Provider Document Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_provider_documents_service_provider_id ON provider_documents(service_provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_documents_document_type ON provider_documents(document_type);

-- ========================================
-- Customer Profile Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_customer_profiles_user_id ON customer_profiles(user_id);

-- ========================================
-- Service Provider Profile Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_service_provider_profiles_user_id ON service_provider_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_service_provider_profiles_service_type ON service_provider_profiles(service_type);
CREATE INDEX IF NOT EXISTS idx_service_provider_profiles_verification_status ON service_provider_profiles(verification_status);
CREATE INDEX IF NOT EXISTS idx_service_provider_profiles_is_verified ON service_provider_profiles(is_verified);

-- ========================================
-- User Roles Indexes
-- ========================================
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);