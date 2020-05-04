CREATE TABLE accounts
(
    account_id BIGSERIAL PRIMARY KEY,
    start_date TIMESTAMP   NOT NULL DEFAULT NOW(),
    full_name  varchar(64) NOT NULL,
    stop_date  TIMESTAMP
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
