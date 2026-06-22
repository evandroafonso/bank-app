INSERT INTO transactions (
    uuid,
    account_id,
    source_currency,
    target_currency,
    source_amount,
    converted_amount,
    exchange_rate,
    balance_after,
    type,
    description,
    created_at,
    updated_at
)
VALUES
    (RANDOM_UUID(), 1, 'EUR', 'EUR', 12.1400, 12.1400, 1.00000000, 12.1400, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 1, 'EUR', 'EUR', 14.9400, 14.9400, 1.00000000, 27.0800, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 1, 'EUR', 'EUR', 11.3420, 11.3420, 1.00000000, 38.4220, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 1, 'EUR', 'EUR', 2.3324,  2.3324,  1.00000000, 40.7544, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 1, 'EUR', 'EUR', 13.8400, 13.8400, 1.00000000, 54.5944, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 1, 'EUR', 'EUR', 17.7700, 17.7700, 1.00000000, 72.3644, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 1, 'EUR ','EUR', 18.4400, 18.4400, 1.00000000, 90.8044, 'CREDIT', 'Test transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);