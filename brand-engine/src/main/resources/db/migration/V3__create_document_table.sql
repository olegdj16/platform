CREATE TABLE documents (
                           id UUID PRIMARY KEY,
                           project_id UUID NOT NULL,
                           name VARCHAR(150) NOT NULL,
                           original_filename VARCHAR(255) NOT NULL,
                           content_type VARCHAR(150),
                           storage_key VARCHAR(500),
                           size_bytes BIGINT,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                           CONSTRAINT fk_documents_project
                               FOREIGN KEY (project_id)
                                   REFERENCES projects(id)
                                   ON DELETE CASCADE
);

CREATE INDEX idx_documents_project_id
    ON documents(project_id);