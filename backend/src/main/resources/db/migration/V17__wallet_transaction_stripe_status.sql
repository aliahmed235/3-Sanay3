ALTER TABLE wallet_transactions ADD COLUMN stripe_payment_intent_id VARCHAR(255);
ALTER TABLE wallet_transactions ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';
