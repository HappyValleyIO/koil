ALTER TABLE accounts
    ADD COLUMN weekly_activity         BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN email_on_account_change BOOLEAN NOT NULL DEFAULT true;
