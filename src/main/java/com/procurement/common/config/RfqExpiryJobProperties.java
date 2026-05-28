package com.procurement.common.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jobs.rfq-expiry")
public record RfqExpiryJobProperties(
  boolean enabled,

  @Min(value = 1000, message = "RFQ expiry fixed delay must be at least 1000 ms") long fixedDelayMs,

  @Min(value = 0, message = "RFQ expiry initial delay must not be negative") long initialDelayMs,

  @Min(value = 1, message = "RFQ expiry batch size must be at least 1") int batchSize
) {}
