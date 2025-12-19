package com.example.timelock.release;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Release templates allow users to create releases from pre-configured templates
 * with default values for common release patterns.
 */
@Entity
@Table(name = "release_templates")
public class ReleaseTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private String defaultTitle;

  @Column(columnDefinition = "TEXT")
  private String defaultDescription;

  @Column(columnDefinition = "TEXT")
  private String defaultPayloadJson;

  @Column(nullable = false)
  private String createdBy;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  private boolean active = true;

  // Getters and setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getDefaultTitle() { return defaultTitle; }
  public void setDefaultTitle(String defaultTitle) { this.defaultTitle = defaultTitle; }

  public String getDefaultDescription() { return defaultDescription; }
  public void setDefaultDescription(String defaultDescription) { this.defaultDescription = defaultDescription; }

  public String getDefaultPayloadJson() { return defaultPayloadJson; }
  public void setDefaultPayloadJson(String defaultPayloadJson) { this.defaultPayloadJson = defaultPayloadJson; }

  public String getCreatedBy() { return createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}


