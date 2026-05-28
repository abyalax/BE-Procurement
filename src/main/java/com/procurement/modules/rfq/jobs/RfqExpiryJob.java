package com.procurement.modules.rfq.jobs;

import com.procurement.common.config.RfqExpiryJobProperties;
import com.procurement.common.request.RequestUser;
import com.procurement.modules.rfq.repositories.RfqRepository;
import com.procurement.modules.rfq.services.RfqService;
import com.procurement.modules.workflow.dto.DecisionRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RfqExpiryJob {

  private static final Logger log = LoggerFactory.getLogger(RfqExpiryJob.class);
  private static final RequestUser SYSTEM_ACTOR = new RequestUser(
    null,
    "System",
    "system@procurement.local",
    null,
    null,
    null,
    null,
    null
  );
  private static final String EXPIRY_NOTES = "RFQ deadline expired automatically";

  private final RfqExpiryJobProperties properties;
  private final RfqRepository rfqRepository;
  private final RfqService rfqService;

  @Scheduled(
    fixedDelayString = "${app.jobs.rfq-expiry.fixed-delay-ms:60000}",
    initialDelayString = "${app.jobs.rfq-expiry.initial-delay-ms:10000}"
  )
  void expireOverdueRfqs() {
    if (!properties.enabled()) {
      log.debug("RFQ expiry job is disabled");
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    List<Long> rfqIds = rfqRepository.findExpiredActiveIds(
      now,
      PageRequest.of(0, properties.batchSize())
    );

    if (rfqIds.isEmpty()) {
      log.debug("RFQ expiry job found no overdue RFQs");
      return;
    }

    log.info("RFQ expiry job started for {} overdue RFQs", rfqIds.size());
    int expiredCount = 0;
    int failedCount = 0;

    for (Long rfqId : rfqIds) {
      try {
        rfqService.expire(rfqId, new DecisionRequest(SYSTEM_ACTOR, EXPIRY_NOTES));
        expiredCount++;
      } catch (RuntimeException exception) {
        failedCount++;
        log.error("RFQ expiry job failed for RFQ {}", rfqId, exception);
      }
    }

    log.info(
      "RFQ expiry job finished: expired={}, failed={}, total={}",
      expiredCount,
      failedCount,
      rfqIds.size()
    );
  }
}
