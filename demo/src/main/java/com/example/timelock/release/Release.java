package com.example.timelock.release;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "releases")
public class Release {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false) private String title;

  // Use TEXT (≈64KB). If you need more, use LONGTEXT instead.
  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")   // or "LONGTEXT" if payloads can be large
  private String payloadJson;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false) private ReleaseStatus status = ReleaseStatus.DRAFT;

  private Instant scheduledAt;

  @Column(nullable=false) private String createdBy;
  @Column(nullable=false) private Instant createdAt = Instant.now();

  private String approvedBy;
  private Instant approvedAt;
  private Instant executedAt;

  // getters/setters …
  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getPayloadJson() { return payloadJson; }
  public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
  public ReleaseStatus getStatus() { return status; }
  public void setStatus(ReleaseStatus status) { this.status = status; }
  public Instant getScheduledAt() { return scheduledAt; }
  public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
  public String getCreatedBy() { return createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public String getApprovedBy() { return approvedBy; }
  public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
  public Instant getApprovedAt() { return approvedAt; }
  public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
  public Instant getExecutedAt() { return executedAt; }
  public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}