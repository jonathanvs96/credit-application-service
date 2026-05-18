
CREATE TABLE credit_applications (
    id                      UUID            PRIMARY KEY,
    customer_name           VARCHAR(120)    NOT NULL,
    customer_email          VARCHAR(160)    NOT NULL,
    customer_rfc            VARCHAR(13),
    requested_amount        NUMERIC(15,2)   NOT NULL CHECK (requested_amount > 0),
    currency                CHAR(3)         NOT NULL DEFAULT 'MXN',
    term_months             INTEGER         NOT NULL CHECK (term_months BETWEEN 6 AND 60),
    annual_interest_rate    NUMERIC(5,4)    NOT NULL,
    monthly_payment         NUMERIC(15,2)   NOT NULL,
    total_to_pay            NUMERIC(15,2)   NOT NULL,
    amount_usd              NUMERIC(15,2),
    amount_eur              NUMERIC(15,2),
    exchange_rate_date      DATE,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'CREATED',
    status_reason           TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_status   CHECK (
        status IN (
            'CREATED',
            'UNDER_REVIEW',
            'APPROVED',
            'REJECTED',
            'CANCELLED'
        )
    )
);

CREATE INDEX idx_credit_applications_status     ON credit_applications(status);
CREATE INDEX idx_credit_applications_rfc        ON credit_applications(customer_rfc);
CREATE INDEX idx_credit_applications_created_at ON credit_applications(created_at DESC);