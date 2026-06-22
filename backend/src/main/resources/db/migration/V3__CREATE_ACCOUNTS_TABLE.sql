CREATE TABLE accounts
(
    id         SERIAL PRIMARY KEY,
    uuid       UUID           NOT NULL UNIQUE,
    IBAN       VARCHAR(50)    NOT NULL UNIQUE,
    currency   VARCHAR(10)    NOT NULL,
    balance    DECIMAL(19, 4) NOT NULL,
    user_id    BIGINT         NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,

    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);