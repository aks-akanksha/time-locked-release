package com.example.timelock.release;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
  // Optional: only if you added the DueExecutor background job
  List<Release> findTop50ByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
      ReleaseStatus status, Instant before);
}
