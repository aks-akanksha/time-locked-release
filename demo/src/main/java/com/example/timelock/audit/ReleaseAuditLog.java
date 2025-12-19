package com.example.timelock.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "release_audit_log")
public class ReleaseAuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long releaseId;

  @Column(nullable = false)
  private String action; // CREATED, SCHEDULED, APPROVED, EXECUTED, CANCELLED

  @Column(nullable = false)
  private String performedBy;

  @Column(nullable = false)
  private Instant performedAt = Instant.now();

  @Column(columnDefinition = "TEXT")
  private String details; // JSON or text details about the action

  // Getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getReleaseId() { return releaseId; }
  public void setReleaseId(Long releaseId) { this.releaseId = releaseId; }

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }

  public String getPerformedBy() { return performedBy; }
  public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

  public Instant getPerformedAt() { return performedAt; }
  public void setPerformedAt(Instant performedAt) { this.performedAt = performedAt; }

  public String getDetails() { return details; }
  public void setDetails(String details) { this.details = details; }
}

