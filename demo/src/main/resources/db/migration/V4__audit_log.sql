-- Release audit log table
CREATE TABLE IF NOT EXISTS release_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  release_id BIGINT NOT NULL,
  action VARCHAR(32) NOT NULL,
  performed_by VARCHAR(255) NOT NULL,
  performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  details TEXT NULL,
  INDEX idx_release_id (release_id),
  INDEX idx_performed_at (performed_at)
);

