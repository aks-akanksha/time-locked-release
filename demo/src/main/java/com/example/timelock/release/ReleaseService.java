package com.example.timelock.release;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timelock.execution.ReleaseActionClient;

import java.time.Instant;

@Service
public class ReleaseService {
  private final ReleaseRepository repo;
  private final ReleaseActionClient releaseActionClient;

  public ReleaseService(ReleaseRepository repo,
                        ReleaseActionClient releaseActionClient) {
      this.repo = repo;
      this.releaseActionClient = releaseActionClient;
  }

  @Transactional
  public Release create(String title, String description, String payloadJson, String createdBy) {
    var r = new Release();
    r.setTitle(title);
    r.setDescription(description);
    r.setPayloadJson(payloadJson);
    r.setCreatedBy(createdBy);
    r.setStatus(ReleaseStatus.DRAFT);
    return repo.save(r);
  }

  @Transactional
  public Release schedule(Long id, Instant when) {
    var r = repo.findById(id).orElseThrow();
    r.setScheduledAt(when);
    r.setStatus(ReleaseStatus.SCHEDULED);
    return r;
  }

  @Transactional
  public Release approve(Long id, String approver) {
    var r = repo.findById(id).orElseThrow();
    r.setApprovedBy(approver);
    r.setApprovedAt(Instant.now());
    r.setStatus(ReleaseStatus.APPROVED);
    return r;
  }

  @Transactional
  public Release executeRelease(long id, String actorEmail) {
    var r = repo.findById(id).orElseThrow(() -> new NoSuchElementException("release " + id));
    if (r.getStatus() != ReleaseStatus.APPROVED) {
      throw new IllegalStateException("Release must be APPROVED before execution");
    }
    if (r.getScheduledAt() == null || Instant.now().isBefore(r.getScheduledAt())) {
      throw new IllegalStateException("Cannot execute before scheduledAt");
    }
    r.setStatus(ReleaseStatus.EXECUTED);
    r.setExecutedAt(Instant.now());
    repo.save(r);

    try {
        releaseActionClient.trigger(r.getId(), r.getTitle(), r.getPayloadJson());
    } 
    catch (Exception e) {
        System.err.println("Webhook call failed: " + e.getMessage());
    }

    return r;
  }
}