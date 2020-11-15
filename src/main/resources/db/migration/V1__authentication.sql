CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE accounts
(
    account_id        BIGSERIAL PRIMARY KEY,
    start_date        TIMESTAMP   NOT NULL DEFAULT NOW(),
    full_name         varchar(64) NOT NULL,
    handle            VARCHAR(16) NOT NULL,
    public_account_id UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    stop_date         TIMESTAMP
);

CREATE TABLE account_credentials
(
    account_id    BIGINT PRIMARY KEY NOT NULL REFERENCES accounts (account_id) ON DELETE CASCADE,
    email_address varchar(254)       NOT NULL UNIQUE,
    password      varchar(254)       NOT NULL
);

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

CREATE TABLE auth_authorities
(
    authority_id      SERIAL PRIMARY KEY NOT NULL,
    authority_ref     VARCHAR(32)        NOT NULL UNIQUE,
    authority_created TIMESTAMP          NOT NULL DEFAULT NOW()
);

INSERT INTO auth_authorities
    (authority_ref)
VALUES ('ADMIN');

INSERT INTO auth_authorities
    (authority_ref)
VALUES ('USER');

CREATE TABLE account_authorities
(
    account_id        BIGINT    NOT NULL REFERENCES accounts (account_id) ON DELETE CASCADE,
    authority_id      INT       NOT NULL REFERENCES auth_authorities (authority_id),
    authority_granted TIMESTAMP NOT NULL DEFAULT NOW()
);
