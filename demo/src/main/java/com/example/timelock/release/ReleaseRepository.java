package com.example.timelock.release;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {
  // For background job
  List<Release> findTop50ByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
      ReleaseStatus status, Instant before);
  
  // Search by title or description
  Page<Release> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
      String title, String description, Pageable pageable);
  
  // Filter by status
  Page<Release> findByStatus(ReleaseStatus status, Pageable pageable);
  
  // Filter by status and search
  @Query("SELECT r FROM Release r WHERE r.status = :status " +
         "AND (LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
         "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<Release> findByStatusAndSearch(@Param("status") ReleaseStatus status, 
                                       @Param("search") String search, 
                                       Pageable pageable);
  
  // Search only
  @Query("SELECT r FROM Release r WHERE " +
         "LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
         "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Release> findBySearch(@Param("search") String search, Pageable pageable);
  
  // Count by status
  long countByStatus(ReleaseStatus status);
}
