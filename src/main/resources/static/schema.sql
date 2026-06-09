-- ==========================================
-- 1. USERS TABLE
-- ==========================================
DROP SEQUENCE IF EXISTS simple_user_seq CASCADE;
DROP TABLE IF EXISTS simple_user CASCADE;
CREATE SEQUENCE simple_user_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE simple_user
(
    id         BIGINT PRIMARY KEY           DEFAULT nextval('simple_user_seq'),
    username   VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    email      VARCHAR(255),
    is_enabled BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE     DEFAULT timezone('utc', now()),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- ==========================================
-- 2. TOKENS TABLE (1:Many relationship)
-- ==========================================
DROP SEQUENCE IF EXISTS simple_token_seq CASCADE;
DROP TABLE IF EXISTS simple_token CASCADE;
CREATE SEQUENCE simple_token_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE simple_token
(
    id         BIGINT PRIMARY KEY       DEFAULT nextval('simple_token_seq'),
    user_id    BIGINT                   NOT NULL,
    token      TEXT                     NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN                  DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now()),
    CONSTRAINT fk_user_token FOREIGN KEY (user_id) REFERENCES simple_user (id) ON DELETE CASCADE
);

-- ==========================================
-- 3. ROLES TABLE
-- ==========================================
DROP SEQUENCE IF EXISTS simple_role_seq CASCADE;
DROP TABLE IF EXISTS simple_role CASCADE;
CREATE SEQUENCE simple_role_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE simple_role
(
    id         BIGINT PRIMARY KEY       DEFAULT nextval('simple_role_seq'),
    role       VARCHAR(64) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc', now())
);

-- ==========================================
-- 4. USER-ROLES JOIN TABLE (Many:Many)
-- ==========================================
-- Note: Removed the sequence. Using a composite primary key instead.
DROP TABLE IF EXISTS simple_user_role CASCADE;
CREATE TABLE simple_user_role
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),                                                         -- Prevents duplicate user-role assignments
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES simple_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES simple_role (id) ON DELETE CASCADE -- Fixed table name reference
);