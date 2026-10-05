-- 1. Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    authentication_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- Indexing for Users (Check if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_users_email' AND n.nspname = 'public') THEN
CREATE INDEX idx_users_email ON users(email);
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_users_username' AND n.nspname = 'public') THEN
CREATE INDEX idx_users_username ON users(username);
END IF;
END $$;

-- 2. Create Signup OTP Table
CREATE TABLE IF NOT EXISTS signup_otp (
                                          id BIGSERIAL PRIMARY KEY,
                                          email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL,
    exp_time TIMESTAMP WITH TIME ZONE NOT NULL,
    attempts INT DEFAULT 0,
    resend_count INT DEFAULT 0,
    lock_until TIMESTAMP WITH TIME ZONE,
    verified_at TIMESTAMP WITH TIME ZONE,
                              ip_addr VARCHAR(100),
    user_agent TEXT,
    last_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                              );

-- Indexing for Signup OTP
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_signup_otp_email' AND n.nspname = 'public') THEN
CREATE INDEX idx_signup_otp_email ON signup_otp(email);
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_signup_otp_status' AND n.nspname = 'public') THEN
CREATE INDEX idx_signup_otp_status ON signup_otp(status);
END IF;
END $$;

-- 3. Create Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     email VARCHAR(255) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_time TIMESTAMP WITH TIME ZONE NOT NULL,
    lock_until TIMESTAMP WITH TIME ZONE,
                             resend_count INT DEFAULT 0,
                             ip_address VARCHAR(100),
    user_agent TEXT,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                             );

-- Indexing for Password Reset
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_pwd_reset_email' AND n.nspname = 'public') THEN
CREATE INDEX idx_pwd_reset_email ON password_reset_tokens(email);
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = 'idx_pwd_reset_token' AND n.nspname = 'public') THEN
CREATE INDEX idx_pwd_reset_token ON password_reset_tokens(token);
END IF;
END $$;