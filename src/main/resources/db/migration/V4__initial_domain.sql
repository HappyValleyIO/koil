CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE accounts
    ADD COLUMN public_account_id UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4();

CREATE TABLE cycle_frequency
(
    frequency_id SERIAL PRIMARY KEY NOT NULL,
    unique_ref   VARCHAR(16) UNIQUE NOT NULL
);

INSERT INTO cycle_frequency
    (unique_ref)
VALUES ('DAILY'),
       ('WEEKLY'),
       ('MONTHLY'),
       ('QUARTERLY'),
       ('ANNUALLY');

CREATE TABLE retrospective_cycles
(
    retrospective_cycle_id BIGSERIAL PRIMARY KEY,
    public_cycle_id UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    title VARCHAR(140) NOT NULL,
    description TEXT NOT NULL,
    cycle_frequency_id INT NOT NULL REFERENCES cycle_frequency(frequency_id),
    template TEXT NOT NULL DEFAULT '',
    created_by BIGINT NOT NULL REFERENCES accounts(account_id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_public BOOLEAN NOT NULL DEFAULT false,
    default_reminder_email BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE retrospectives
(
    retrospective_id BIGSERIAL PRIMARY KEY,
    retrospective_author_id BIGINT REFERENCES accounts(account_id),
    public_retrospective_id UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    retrospective_cycle_id BIGINT REFERENCES retrospective_cycles(retrospective_cycle_id),
    content TEXT NOT NULL,
    is_public BOOLEAN NOT NULL,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_draft BOOLEAN NOT NULL DEFAULT true
);
