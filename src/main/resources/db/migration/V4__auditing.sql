ALTER TABLE accounts
    ADD COLUMN last_modified_by BIGINT REFERENCES accounts (account_id);
