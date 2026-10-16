CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    balance BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT nonnegative_balance CHECK (balance >= 0)
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL CHECK (amount > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
