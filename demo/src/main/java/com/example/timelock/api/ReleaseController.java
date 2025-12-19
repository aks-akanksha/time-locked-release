package com.example.timelock.api;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.example.timelock.api.dto.AuditLogDto;
import com.example.timelock.api.dto.CreateReleaseDto;
import com.example.timelock.api.dto.PageResponse;
import com.example.timelock.api.dto.ReleaseResponseDto;
import com.example.timelock.api.dto.ReleaseStatisticsDto;
import com.example.timelock.api.dto.ScheduleDto;
import com.example.timelock.audit.ReleaseAuditLogRepository;
import com.example.timelock.release.Release;
import com.example.timelock.release.ReleaseRepository;
import com.example.timelock.release.ReleaseService;
import com.example.timelock.release.ReleaseStatus;

import jakarta.validation.Valid;

@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/releases")
public class ReleaseController {
  private final ReleaseRepository repo;
  private final ReleaseService svc;
  private final ReleaseAuditLogRepository auditLogRepository;

  public ReleaseController(ReleaseRepository repo, ReleaseService svc,
                           ReleaseAuditLogRepository auditLogRepository) {
    this.repo = repo; 
    this.svc = svc;
    this.auditLogRepository = auditLogRepository;
  }

  @GetMapping
  public PageResponse<ReleaseResponseDto> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) ReleaseStatus status,
      @RequestParam(required = false) String search) {
    
    Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt");
    if (sortDir != null && sortDir.equalsIgnoreCase("asc")) {
      sort = sort.ascending();
    } else {
      sort = sort.descending();
    }
    
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Release> releasePage;
    
    if (search != null && !search.trim().isEmpty()) {
      if (status != null) {
        releasePage = repo.findByStatusAndSearch(status, search.trim(), pageable);
      } else {
        releasePage = repo.findBySearch(search.trim(), pageable);
      }
    } else if (status != null) {
      releasePage = repo.findByStatus(status, pageable);
    } else {
      releasePage = repo.findAll(pageable);
    }
    
    List<ReleaseResponseDto> content = releasePage.getContent().stream()
        .map(ReleaseResponseDto::from)
        .collect(Collectors.toList());
    
    return PageResponse.of(
        content,
        releasePage.getNumber(),
        releasePage.getSize(),
        releasePage.getTotalElements()
    );
  }

  @GetMapping("/{id}")
  public ReleaseResponseDto getById(@PathVariable Long id) {
    Release release = svc.findById(id);
    return ReleaseResponseDto.from(release);
  }

  @GetMapping("/statistics")
  public ReleaseStatisticsDto getStatistics() {
    long total = repo.count();
    Map<String, Long> byStatus = Map.of(
        "DRAFT", repo.countByStatus(ReleaseStatus.DRAFT),
        "SCHEDULED", repo.countByStatus(ReleaseStatus.SCHEDULED),
        "APPROVED", repo.countByStatus(ReleaseStatus.APPROVED),
        "EXECUTED", repo.countByStatus(ReleaseStatus.EXECUTED),
        "CANCELLED", repo.countByStatus(ReleaseStatus.CANCELLED)
    );
    
    return new ReleaseStatisticsDto(
        total,
        byStatus,
        repo.countByStatus(ReleaseStatus.SCHEDULED),
        repo.countByStatus(ReleaseStatus.APPROVED),
        repo.countByStatus(ReleaseStatus.EXECUTED),
        repo.countByStatus(ReleaseStatus.CANCELLED)
    );
  }

  @PostMapping
  public ReleaseResponseDto create(@Valid @RequestBody CreateReleaseDto dto, Principal principal) {
    String createdBy = principal != null ? principal.getName() : "unknown";
    Release release = svc.create(
        dto.title(),
        dto.description(),
        dto.payloadJson(),
        createdBy
    );
    return ReleaseResponseDto.from(release);
  }

  @PostMapping("/{id}/actions/schedule")
  public ReleaseResponseDto schedule(@PathVariable Long id,
                          @Valid @RequestBody ScheduleDto body) {
    if (body.scheduledAt() == null) {
      throw new IllegalArgumentException("scheduledAt is required");
    }
    Instant when = body.scheduledAt().toInstant();
    Release release = svc.schedule(id, when);
    return ReleaseResponseDto.from(release);
  }

  @PostMapping("/{id}/actions/approve")
  public ReleaseResponseDto approve(@PathVariable("id") Long id, Authentication auth) {
    String approver = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
    Release release = svc.approve(id, approver);
    return ReleaseResponseDto.from(release);
  }
    
  @PostMapping("/{id}/actions/execute")
  public ReleaseResponseDto execute(@PathVariable Long id, Authentication auth) {
    String executor = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
    Release release = svc.executeRelease(id, executor);
    return ReleaseResponseDto.from(release);
  }

  @PostMapping("/{id}/actions/cancel")
  public ReleaseResponseDto cancel(@PathVariable Long id, Authentication auth) {
    String cancelledBy = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
    Release release = svc.cancel(id, cancelledBy);
    return ReleaseResponseDto.from(release);
  }

  @GetMapping("/{id}/history")
  public PageResponse<AuditLogDto> getHistory(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var auditPage = auditLogRepository.findByReleaseIdOrderByPerformedAtDesc(id, pageable);
    
    List<AuditLogDto> content = auditPage.getContent().stream()
        .map(AuditLogDto::from)
        .collect(Collectors.toList());
    
    return PageResponse.of(
        content,
        auditPage.getNumber(),
        auditPage.getSize(),
        auditPage.getTotalElements()
    );
  }
}