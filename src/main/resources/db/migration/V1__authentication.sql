CREATE
EXTENSION IF NOT EXISTS "uuid-ossp" schema public;

CREATE TABLE company
(
    company_id   BIGSERIAL PRIMARY KEY,
    company_name varchar(255) NOT NULL,
    start_date   TIMESTAMP         NOT NULL DEFAULT NOW(),
    stop_date    TIMESTAMP,
    signup_link  UUID UNIQUE  NOT NULL
);

CREATE TABLE accounts
(
    account_id                BIGSERIAL PRIMARY KEY,
    company_id                BIGINT       NOT NULL,
    start_date                TIMESTAMP    NOT NULL DEFAULT NOW(),
    full_name                 varchar(64)  NOT NULL,
    handle                    VARCHAR(16)  NOT NULL,
    public_account_id         UUID UNIQUE  NOT NULL DEFAULT public.uuid_generate_v4(),
    email_address             varchar(254) NOT NULL UNIQUE,
    password                  varchar(254) NOT NULL,
    verification_requested_at TIMESTAMP    NOT NULL,
    verification_code         UUID UNIQUE  NOT NULL,
    verified_at               TIMESTAMP,
    stop_date                 TIMESTAMP,
    CONSTRAINT account_company_fk0 FOREIGN KEY (company_id) REFERENCES company (company_id) ON DELETE CASCADE
);

CREATE INDEX ON accounts (email_address);

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
