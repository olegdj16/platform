CREATE TABLE projects (
                          id UUID PRIMARY KEY,
                          organization_id UUID NOT NULL,
                          name VARCHAR(150) NOT NULL,
                          description VARCHAR(1000),
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_projects_organization
                              FOREIGN KEY (organization_id)
                                  REFERENCES organizations (id)
                                  ON DELETE CASCADE
);

CREATE INDEX ix_projects_organization_id
    ON projects (organization_id);

CREATE UNIQUE INDEX ux_projects_organization_name
    ON projects (organization_id, LOWER(name));