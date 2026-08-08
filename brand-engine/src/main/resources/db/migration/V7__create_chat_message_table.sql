CREATE TABLE chat_message (
                              id UUID PRIMARY KEY,
                              conversation_id UUID NOT NULL,
                              role VARCHAR(50) NOT NULL,
                              content TEXT NOT NULL,
                              created_at TIMESTAMP NOT NULL,

                              CONSTRAINT fk_chat_message_conversation
                                  FOREIGN KEY (conversation_id)
                                      REFERENCES conversation(id)
                                      ON DELETE CASCADE
);

CREATE INDEX idx_chat_message_conversation_id
    ON chat_message(conversation_id);