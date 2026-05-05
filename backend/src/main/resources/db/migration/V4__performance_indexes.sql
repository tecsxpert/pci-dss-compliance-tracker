-- V4__performance_indexes.sql
-- Adding performance indexes (Flyway mixed mode required)
-- spring.flyway.mixed=true

-- Composite index on (status, due_date) for overdue and deadline queries
CREATE INDEX CONCURRENTLY idx_compliance_records_status_due_date ON compliance_records (status, due_date);

-- Composite index on (assigned_to, status) for assignment filter queries
CREATE INDEX CONCURRENTLY idx_compliance_records_assigned_to_status ON compliance_records (assigned_to, status);

-- Partial index on due_date for active (non-deleted) records
CREATE INDEX CONCURRENTLY idx_compliance_records_due_date_active ON compliance_records (due_date) WHERE is_deleted = false;

-- Index on compliance_score for statistics calculations
CREATE INDEX CONCURRENTLY idx_compliance_records_compliance_score ON compliance_records (compliance_score);

-- Index on created_at for default pagination sorting
CREATE INDEX CONCURRENTLY idx_compliance_records_created_at ON compliance_records (created_at);
