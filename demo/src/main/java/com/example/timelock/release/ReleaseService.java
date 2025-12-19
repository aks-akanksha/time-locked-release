package com.example.timelock.release;

import com.example.timelock.audit.AuditService;
import com.example.timelock.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timelock.execution.ReleaseActionClient;

import java.time.Instant;

@Service
public class ReleaseService {
  private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);
  private final ReleaseRepository repo;
  private final ReleaseActionClient releaseActionClient;
  private final AuditService auditService;

  public ReleaseService(ReleaseRepository repo,
                        ReleaseActionClient releaseActionClient,
                        AuditService auditService) {
      this.repo = repo;
      this.releaseActionClient = releaseActionClient;
      this.auditService = auditService;
  }

  @Transactional
  public Release create(String title, String description, String payloadJson, String createdBy) {
    log.info("Creating release: {} by {}", title, createdBy);
    var r = new Release();
    r.setTitle(title);
    r.setDescription(description);
    r.setPayloadJson(payloadJson);
    r.setCreatedBy(createdBy);
    r.setStatus(ReleaseStatus.DRAFT);
    Release saved = repo.save(r);
    auditService.logAction(saved.getId(), "CREATED", createdBy, 
        String.format("Title: %s", title));
    log.info("Created release with id: {}", saved.getId());
    return saved;
  }

  @Transactional
  public Release schedule(Long id, Instant when) {
    log.info("Scheduling release {} for {}", id, when);
    var r = repo.findById(id)
        .orElseThrow(() -> new ReleaseNotFoundException(id));
    
    if (r.getStatus() == ReleaseStatus.EXECUTED) {
      throw new ReleaseAlreadyExecutedException(id);
    }
    if (r.getStatus() == ReleaseStatus.CANCELLED) {
      throw new ReleaseCancelledException(id);
    }
    
    r.setScheduledAt(when);
    r.setStatus(ReleaseStatus.SCHEDULED);
    Release saved = repo.save(r);
    auditService.logAction(id, "SCHEDULED", "system", 
        String.format("Scheduled for: %s", when));
    log.info("Scheduled release {} for {}", id, when);
    return saved;
  }

  @Transactional
  public Release approve(Long id, String approver) {
    log.info("Approving release {} by {}", id, approver);
    var r = repo.findById(id)
        .orElseThrow(() -> new ReleaseNotFoundException(id));
    
    if (r.getStatus() == ReleaseStatus.EXECUTED) {
      throw new ReleaseAlreadyExecutedException(id);
    }
    if (r.getStatus() == ReleaseStatus.CANCELLED) {
      throw new ReleaseCancelledException(id);
    }
    
    r.setApprovedBy(approver);
    r.setApprovedAt(Instant.now());
    r.setStatus(ReleaseStatus.APPROVED);
    Release saved = repo.save(r);
    auditService.logAction(id, "APPROVED", approver, null);
    log.info("Approved release {} by {}", id, approver);
    return saved;
  }

  @Transactional
  public Release cancel(Long id, String cancelledBy) {
    log.info("Cancelling release {} by {}", id, cancelledBy);
    var r = repo.findById(id)
        .orElseThrow(() -> new ReleaseNotFoundException(id));
    
    if (r.getStatus() == ReleaseStatus.EXECUTED) {
      throw new ReleaseAlreadyExecutedException(id);
    }
    if (r.getStatus() == ReleaseStatus.CANCELLED) {
      throw new ReleaseCancelledException(id);
    }
    
    r.setStatus(ReleaseStatus.CANCELLED);
    Release saved = repo.save(r);
    auditService.logAction(id, "CANCELLED", cancelledBy, null);
    log.info("Cancelled release {} by {}", id, cancelledBy);
    return saved;
  }

  @Transactional
  public Release executeRelease(long id, String actorEmail) {
    log.info("Executing release {} by {}", id, actorEmail);
    var r = repo.findById(id)
        .orElseThrow(() -> new ReleaseNotFoundException(id));
    
    if (r.getStatus() == ReleaseStatus.EXECUTED) {
      throw new ReleaseAlreadyExecutedException(id);
    }
    if (r.getStatus() == ReleaseStatus.CANCELLED) {
      throw new ReleaseCancelledException(id);
    }
    if (r.getStatus() != ReleaseStatus.APPROVED) {
      throw new ReleaseNotApprovedException(id);
    }
    if (r.getScheduledAt() == null) {
      throw new ReleaseNotScheduledException(id);
    }
    if (Instant.now().isBefore(r.getScheduledAt())) {
      throw new ReleaseExecutionTooEarlyException(id, r.getScheduledAt());
    }
    
    r.setStatus(ReleaseStatus.EXECUTED);
    r.setExecutedAt(Instant.now());
    Release saved = repo.save(r);
    
    auditService.logAction(id, "EXECUTED", actorEmail != null ? actorEmail : "system", null);

    try {
        log.info("Triggering webhook for release {}", id);
        releaseActionClient.trigger(r.getId(), r.getTitle(), r.getPayloadJson());
        log.info("Webhook triggered successfully for release {}", id);
    } 
    catch (Exception e) {
        log.error("Webhook call failed for release {}: {}", id, e.getMessage(), e);
        // Don't fail the execution if webhook fails
    }

    log.info("Successfully executed release {}", id);
    return saved;
  }

  public Release findById(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ReleaseNotFoundException(id));
  }
}