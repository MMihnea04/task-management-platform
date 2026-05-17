-- Adaugam prioritatea (implicit: MEDIUM)
ALTER TABLE tasks ADD COLUMN priority VARCHAR(20) DEFAULT 'MEDIUM';

-- Adaugam flag-ul de atentionare (implicit: FALSE)
ALTER TABLE tasks ADD COLUMN needs_attention BOOLEAN DEFAULT FALSE;

-- Adaugam timestamp-ul pt monitorizarea timpului in status
ALTER TABLE tasks ADD COLUMN status_changed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();