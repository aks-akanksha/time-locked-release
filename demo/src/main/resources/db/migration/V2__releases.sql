-- Release workflow MVP
CREATE TABLE IF NOT EXISTS releases (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT NULL,
  payload_json TEXT NULL,            -- keep as TEXT for portability
  status VARCHAR(32) NOT NULL,
  scheduled_at TIMESTAMP NULL,
  created_by VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approved_by VARCHAR(255) NULL,
  approved_at TIMESTAMP NULL,
  executed_at TIMESTAMP NULL
);
