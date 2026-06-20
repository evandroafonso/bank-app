CREATE TABLE users
(
    id            SERIAL PRIMARY KEY,
    uuid          UUID         NOT NULL,
    username      VARCHAR(100) NOT NULL,
    personal_id   VARCHAR(11)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);