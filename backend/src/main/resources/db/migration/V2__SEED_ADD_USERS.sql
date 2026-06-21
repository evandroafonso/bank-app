INSERT INTO users (
    uuid,
    username,
    personal_id,
    email,
    password_hash,
    created_at,
    updated_at
) VALUES
      (RANDOM_UUID(), 'John User', '12345678913', 'john@email.com', '$2a$10$l7G9258dExxqkO2qkfuaROSs8Bz7AcmBoZD7CU9D5LG4EUgmoNTF2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (RANDOM_UUID(), 'Jane Doe', '98765432101', 'jane@email.com', '$2a$10$l7G9258dExxqkO2qkfuaROSs8Bz7AcmBoZD7CU9D5LG4EUgmoNTF2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
      (RANDOM_UUID(), 'Admin User', '55566677788', 'admin@email.com', '$2a$10$l7G9258dExxqkO2qkfuaROSs8Bz7AcmBoZD7CU9D5LG4EUgmoNTF2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);