INSERT INTO accounts (uuid,
                      iban,
                      currency,
                      balance,
                      user_id,
                      created_at,
                      updated_at)
VALUES (RANDOM_UUID(), 'EE382200221020145685', 'EUR', 90.8044, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (RANDOM_UUID(), 'EE382200221020145686', 'EUR', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (RANDOM_UUID(), 'EE382200221020145687', 'USD', 0, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);