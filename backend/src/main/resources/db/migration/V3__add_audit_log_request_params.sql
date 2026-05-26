SET @request_params_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'audit_logs'
      AND COLUMN_NAME = 'request_params'
);

SET @add_request_params_sql = IF(
    @request_params_column_exists = 0,
    'ALTER TABLE audit_logs ADD COLUMN request_params MEDIUMTEXT NULL AFTER action_name',
    'SELECT 1'
);

PREPARE add_request_params_stmt FROM @add_request_params_sql;
EXECUTE add_request_params_stmt;
DEALLOCATE PREPARE add_request_params_stmt;
