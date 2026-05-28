package com.procurement.modules.rfq.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.procurement.common.config.RfqExpiryJobProperties;
import com.procurement.modules.rfq.repositories.RfqRepository;
import com.procurement.modules.rfq.services.RfqService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RfqExpiryJobTest {

  @Mock
  private RfqRepository rfqRepository;

  @Mock
  private RfqService rfqService;

  @Test
  void doesNothingWhenDisabled() {
    RfqExpiryJob job = newJob(false, 10);

    job.expireOverdueRfqs();

    verifyNoInteractions(rfqRepository, rfqService);
  }

  @Test
  void expiresOverdueRfqsWithSystemActor() {
    RfqExpiryJob job = newJob(true, 3);
    when(
      rfqRepository.findExpiredActiveIds(any(LocalDateTime.class), eq(PageRequest.of(0, 3)))
    ).thenReturn(List.of(11L, 12L));

    job.expireOverdueRfqs();

    ArgumentCaptor<DecisionRequest> requestCaptor = ArgumentCaptor.forClass(DecisionRequest.class);
    verify(rfqService).expire(eq(11L), requestCaptor.capture());
    verify(rfqService).expire(eq(12L), requestCaptor.capture());

    List<DecisionRequest> requests = requestCaptor.getAllValues();
    requests.forEach(request -> {
      org.assertj.core.api.Assertions.assertThat(request.actorLabel()).isEqualTo(
        "system@procurement.local"
      );
      org.assertj.core.api.Assertions.assertThat(request.notes()).isEqualTo(
        "RFQ deadline expired automatically"
      );
    });
  }

  @Test
  void continuesWhenOneRfqFails() {
    RfqExpiryJob job = newJob(true, 10);
    when(
      rfqRepository.findExpiredActiveIds(any(LocalDateTime.class), eq(PageRequest.of(0, 10)))
    ).thenReturn(List.of(21L, 22L));
    doThrow(new IllegalStateException("boom")).when(rfqService).expire(eq(21L), any());

    job.expireOverdueRfqs();

    verify(rfqService).expire(eq(21L), any());
    verify(rfqService).expire(eq(22L), any());
  }

  @Test
  void skipsServiceWhenNoExpiredRfqsFound() {
    RfqExpiryJob job = newJob(true, 10);
    when(
      rfqRepository.findExpiredActiveIds(any(LocalDateTime.class), eq(PageRequest.of(0, 10)))
    ).thenReturn(List.of());

    job.expireOverdueRfqs();

    verifyNoInteractions(rfqService);
  }

  private RfqExpiryJob newJob(boolean enabled, int batchSize) {
    return new RfqExpiryJob(
      new RfqExpiryJobProperties(enabled, 60000, 10000, batchSize),
      rfqRepository,
      rfqService
    );
  }
}
