CREATE TABLE accounts
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid       UUID           NOT NULL UNIQUE,
    iban       VARCHAR(34)    NOT NULL UNIQUE,
    currency   VARCHAR(3)    NOT NULL,
    balance    DECIMAL(20, 8) NOT NULL,
    user_id    BIGINT         NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,

    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);