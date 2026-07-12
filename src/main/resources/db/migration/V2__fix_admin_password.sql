-- Fix admin password hash to use Spring BCryptPasswordEncoder(12)-generated hash for "Admin@1234"
-- The V1 seed used a pgcrypto-generated hash that is not compatible with jBCrypt.
UPDATE users
SET password_hash = '$2a$12$0i00XmP80rXcMom8k7oiPemFyUKemIxDuaE0VrmHZ99ids4zP2DWK'
WHERE email = 'admin@tels.unza.ac.zm';
