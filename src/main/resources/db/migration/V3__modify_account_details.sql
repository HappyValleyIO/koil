ALTER TABLE accounts
    ADD COLUMN handle VARCHAR(16);

UPDATE accounts
    SET handle = account_id;

ALTER TABLE accounts
    ALTER COLUMN handle SET NOT NULL;
