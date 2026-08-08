ALTER TABLE conversation
    ADD COLUMN user_id UUID NOT NULL;

ALTER TABLE conversation
    ADD CONSTRAINT fk_conversation_user
        FOREIGN KEY (user_id)
            REFERENCES app_users(id);

CREATE INDEX idx_conversation_user_id
    ON conversation(user_id);