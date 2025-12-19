-- Release templates table
CREATE TABLE IF NOT EXISTS release_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT NULL,
  default_title VARCHAR(255) NOT NULL,
  default_description TEXT NULL,
  default_payload_json TEXT NULL,
  created_by VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  INDEX idx_active (active)
);


