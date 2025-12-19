package com.example.timelock.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private static final Logger log = LoggerFactory.getLogger(AuditService.class);
  private final ReleaseAuditLogRepository repository;

  public AuditService(ReleaseAuditLogRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void logAction(Long releaseId, String action, String performedBy, String details) {
    try {
      ReleaseAuditLog logEntry = new ReleaseAuditLog();
      logEntry.setReleaseId(releaseId);
      logEntry.setAction(action);
      logEntry.setPerformedBy(performedBy);
      logEntry.setDetails(details);
      repository.save(logEntry);
      log.debug("Audit log created for release {}: action={}, by={}", releaseId, action, performedBy);
    } catch (Exception e) {
      log.error("Failed to create audit log for release {}: {}", releaseId, e.getMessage(), e);
      // Don't fail the main operation if audit logging fails
    }
  }
}

