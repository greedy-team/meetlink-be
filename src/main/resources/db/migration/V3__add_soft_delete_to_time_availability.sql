ALTER TABLE time_availability ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE time_availability DROP CONSTRAINT uk_time_availability;
