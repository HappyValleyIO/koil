CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE accounts
(
    account_id        BIGSERIAL PRIMARY KEY,
    start_date        TIMESTAMP    NOT NULL DEFAULT NOW(),
    full_name         varchar(64)  NOT NULL,
    handle            VARCHAR(16)  NOT NULL,
    public_account_id UUID UNIQUE  NOT NULL DEFAULT uuid_generate_v4(),
    email_address     varchar(254) NOT NULL UNIQUE,
    password          varchar(254) NOT NULL,
    stop_date         TIMESTAMP
);

CREATE INDEX ON accounts (email_address);

CREATE TABLE account_verification
(
    account_id        BIGINT    NOT NULL REFERENCES accounts (account_id) ON DELETE CASCADE,
    verification_code UUID      NOT NULL UNIQUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at        TIMESTAMP NOT NULL
);

CREATE TABLE account_password_reset
(
    account_id BIGINT    NOT NULL REFERENCES accounts (account_id) ON DELETE CASCADE,
    reset_code UUID      NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE account_authorities
(
    account_id        BIGINT      NOT NULL REFERENCES accounts (account_id) ON DELETE CASCADE,
    accounts_key      BIGINT,
    authority         VARCHAR(16) NOT NULL,
    authority_granted TIMESTAMP   NOT NULL DEFAULT NOW()
);
