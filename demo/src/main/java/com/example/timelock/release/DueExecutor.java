package com.example.timelock.release;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class DueExecutor {
  private static final Logger log = LoggerFactory.getLogger(DueExecutor.class);
  private final ReleaseRepository repo;
  private final ReleaseService svc;

  public DueExecutor(ReleaseRepository repo, ReleaseService svc) {
    this.repo = repo; this.svc = svc;
  }

  @Scheduled(fixedDelay = 5000) // every 5s
  public void tick() {
    var due = repo.findTop50ByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
      ReleaseStatus.APPROVED, Instant.now()
    );
    for (var r : due) {
      try {
        svc.executeRelease(r.getId(), null);
        log.info("Auto-executed release {}", r.getId());
      } catch (Exception e) {
        log.warn("Could not auto-execute {}: {}", r.getId(), e.getMessage());
      }
    }
  }
}
