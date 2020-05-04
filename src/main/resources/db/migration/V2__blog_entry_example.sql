CREATE TABLE blog_entries
(
    blog_entry_id BIGSERIAL PRIMARY KEY NOT NULL,
    account_id    BIGINT                NOT NULL REFERENCES accounts (account_id),
    title         VARCHAR(256)          NOT NULL,
    content       TEXT                  NOT NULL,
    created       TIMESTAMP             NOT NULL DEFAULT NOW()
);
