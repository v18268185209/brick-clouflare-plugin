CREATE TABLE IF NOT EXISTS cf_account (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    display_name TEXT NOT NULL,
    auth_type TEXT NOT NULL,
    api_token_enc TEXT,
    api_key_enc TEXT,
    api_email_enc TEXT,
    cloudflare_account_id TEXT,
    default_zone_id TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    remark TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS cf_operation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER,
    module TEXT,
    action TEXT,
    endpoint TEXT,
    request_body TEXT,
    response_code INTEGER,
    success INTEGER NOT NULL DEFAULT 0,
    message TEXT,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cf_operation_log_account_id ON cf_operation_log(account_id);
CREATE INDEX IF NOT EXISTS idx_cf_operation_log_created_at ON cf_operation_log(created_at);
