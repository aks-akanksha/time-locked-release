package com.example.timelock.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseAuditLogRepository extends JpaRepository<ReleaseAuditLog, Long> {
  Page<ReleaseAuditLog> findByReleaseIdOrderByPerformedAtDesc(Long releaseId, Pageable pageable);
}

