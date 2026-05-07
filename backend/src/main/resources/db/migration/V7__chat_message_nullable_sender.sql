-- V7: Make sender_id nullable in chat_messages for SYSTEM messages (no sender)
ALTER TABLE chat_messages DROP FOREIGN KEY chat_messages_ibfk_2;
ALTER TABLE chat_messages MODIFY sender_id BIGINT NULL;
ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_sender
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT;
