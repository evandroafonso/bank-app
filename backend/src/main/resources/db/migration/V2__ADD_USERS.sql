INSERT INTO users (uuid,
                   username,
                   personal_id,
                   email,
                   password_hash,
                   created_at,
                   updated_at)
VALUES (RANDOM_UUID(),
        'John User',
        '12345678913',
        'john@email.com',
        '$2a$10$l7G9258dExxqkO2qkfuaROSs8Bz7AcmBoZD7CU9D5LG4EUgmoNTF2',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);