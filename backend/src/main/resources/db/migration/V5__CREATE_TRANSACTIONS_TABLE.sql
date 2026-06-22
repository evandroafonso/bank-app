CREATE TABLE transactions
(
    id               SERIAL PRIMARY KEY,
    uuid             UUID NOT NULL UNIQUE,
    account_id       BIGINT NOT NULL,
    source_currency  VARCHAR(10) NOT NULL,
    target_currency  VARCHAR(10) NOT NULL,
    source_amount    DECIMAL(19, 4) NOT NULL,
    converted_amount DECIMAL(19, 4) NOT NULL,
    exchange_rate    DECIMAL(19, 4) NOT NULL,
    balance_after    DECIMAL(19, 4) NOT NULL,
    type             VARCHAR(20) NOT NULL,
    description      VARCHAR(255),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES
        accounts(id)
);