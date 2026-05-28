package com.procurement.modules.rfq.dto;

import com.procurement.modules.rfq.entities.RfqVendorInvitation;
import java.time.LocalDateTime;

public record RfqInvitationResponse(
  Long id,
  Long rfqId,
  Long vendorId,
  String vendorName,
  String status,
  String responseNotes,
  LocalDateTime respondedAt,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static RfqInvitationResponse from(RfqVendorInvitation invitation) {
    return new RfqInvitationResponse(
      invitation.getId(),
      invitation.getRfq().getId(),
      invitation.getVendor().getId(),
      invitation.getVendor().getName(),
      invitation.getStatus(),
      invitation.getResponseNotes(),
      invitation.getRespondedAt(),
      invitation.getCreatedAt(),
      invitation.getUpdatedAt()
    );
  }
}
