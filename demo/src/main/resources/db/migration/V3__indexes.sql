ALTER TABLE releases
  ADD INDEX idx_releases_status_sched (status, scheduled_at);