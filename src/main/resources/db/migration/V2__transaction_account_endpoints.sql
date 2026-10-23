ALTER TABLE transactions
    ADD COLUMN from_account_id UUID REFERENCES accounts(id),
    ADD COLUMN to_account_id UUID REFERENCES accounts(id);

UPDATE transactions
SET from_account_id = CASE WHEN type IN ('WITHDRAWAL', 'TRANSFER') THEN account_id END,
    to_account_id = CASE WHEN type = 'DEPOSIT' THEN account_id END;

ALTER TABLE transactions DROP COLUMN account_id;
