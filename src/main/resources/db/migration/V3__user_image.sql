CREATE TABLE profile_image
(
    id         BIGSERIAL     NOT NULL,
    public_id  UUID UNIQUE   NOT NULL,
    account_id BIGINT UNIQUE NOT NULL REFERENCES accounts (account_id),
    updated_at TIMESTAMP     NOT NULL
);
