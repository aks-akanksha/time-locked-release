package com.example.timelock.release;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import com.example.timelock.execution.ReleaseActionClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReleaseServiceTest {

  @Test
  void mustApproveAndBeDueBeforeExecute() {
    var repo = mock(ReleaseRepository.class);
    var action = mock(ReleaseActionClient.class);
    var svc  = new ReleaseService(repo, action);

    var r = new Release();
    r.setTitle("X");
    ReflectionTestUtils.setField(r, "id", 1L);
    r.setStatus(ReleaseStatus.DRAFT);
    r.setScheduledAt(Instant.now().minusSeconds(60));

    when(repo.findById(1L)).thenReturn(Optional.of(r));

    svc.schedule(1L, r.getScheduledAt());
    assertEquals(ReleaseStatus.SCHEDULED, r.getStatus());

    svc.approve(1L, "approver");
    assertEquals(ReleaseStatus.APPROVED, r.getStatus());
    assertNotNull(r.getApprovedAt());

    svc.executeRelease(1L, "executor");
    assertEquals(ReleaseStatus.EXECUTED, r.getStatus());
    assertNotNull(r.getExecutedAt());

    verify(action, times(1)).trigger(eq(1L), eq("X"), isNull());
  }

  @Test
  void executingTooEarlyFails() {
    var repo = mock(ReleaseRepository.class);
    var action = mock(ReleaseActionClient.class);
    var svc  = new ReleaseService(repo, action);

    var r = new Release();
    r.setTitle("X");
    r.setStatus(ReleaseStatus.APPROVED);
    r.setScheduledAt(Instant.now().plusSeconds(300));
    when(repo.findById(1L)).thenReturn(Optional.of(r));

    var ex = assertThrows(IllegalStateException.class, () -> svc.executeRelease(1L, "runner"));
    assertTrue(ex.getMessage().contains("Cannot execute before"));
  }
}
