CREATE TABLE organizations (
                               id UUID PRIMARY KEY,
                               name VARCHAR(150) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX ux_organizations_name
    ON organizations (LOWER(name));