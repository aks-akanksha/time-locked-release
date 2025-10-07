package com.example.timelock.api;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.example.timelock.api.dto.CreateReleaseDto;
import com.example.timelock.api.dto.ScheduleDto;
import com.example.timelock.release.Release;
import com.example.timelock.release.ReleaseRepository;
import com.example.timelock.release.ReleaseService;

import jakarta.validation.Valid;
import java.security.Principal;

@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/releases")
public class ReleaseController {
  private final ReleaseRepository repo;
  private final ReleaseService svc;

  public ReleaseController(ReleaseRepository repo, ReleaseService svc) {
    this.repo = repo; this.svc = svc;
  }

  @GetMapping
  public List<Release> list() {
    return repo.findAll();
  }

  // @PostMapping
  // public Release create(@RequestBody Map<String,Object> body, Authentication auth) {
  //   String title = (String) body.getOrDefault("title", "Untitled");
  //   String description = (String) body.getOrDefault("description", null);
  //   String payloadJson = (String) body.getOrDefault("payloadJson", null);
  //   String createdBy = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
  //   return svc.create(title, description, payloadJson, createdBy);
  // }

  // @PostMapping("/{id}/actions/schedule")
  // public Release schedule(@PathVariable("id") Long id, @RequestBody Map<String,String> body) {
  //   Instant when = Instant.parse(body.get("scheduledAt")); // ISO-8601
  //   return svc.schedule(id, when);
  // }
  @PostMapping
  public Release create(@Valid @RequestBody CreateReleaseDto dto, Principal principal) {
    String createdBy = principal != null ? principal.getName() : "unknown";
    return svc.create(
        dto.title(),
        dto.description(),
        dto.payloadJson(),
        createdBy
    );
  }


  @PostMapping("{id}/actions/schedule")
  public Release schedule(@PathVariable Long id,
                          @Valid @RequestBody ScheduleDto body) {
    if (body.scheduledAt() == null) {
      throw new IllegalArgumentException("scheduledAt is required");
    }
    Instant when = body.scheduledAt().toInstant();
    return svc.schedule(id, when);
  }


  @PostMapping("/{id}/actions/approve")
  public Release approve(@PathVariable("id") Long id, Authentication auth) {
    String approver = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
    return svc.approve(id, approver);
    }
    
  @PostMapping("{id}/actions/execute")
  public Release execute(@PathVariable Long id, Authentication auth) {
    String executor = auth != null ? String.valueOf(auth.getPrincipal()) : "unknown";
    return svc.executeRelease(id, executor);
  }
}