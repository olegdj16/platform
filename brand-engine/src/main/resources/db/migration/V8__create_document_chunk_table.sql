CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_chunks (
                                 id UUID PRIMARY KEY,
                                 document_id UUID NOT NULL,
                                 chunk_index INTEGER NOT NULL,
                                 content TEXT NOT NULL,
                                 embedding vector(1536),
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_document_chunks_document
                                     FOREIGN KEY (document_id)
                                         REFERENCES documents(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT uq_document_chunk_index
                                     UNIQUE (document_id, chunk_index)
);

CREATE INDEX idx_document_chunks_document_id
    ON document_chunks(document_id);