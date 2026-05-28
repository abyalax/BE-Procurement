package com.procurement.modules.audit.services;

import com.procurement.modules.audit.dto.AuditActivityResponse;
import com.procurement.modules.audit.entities.AuditActivity;
import com.procurement.modules.audit.repositories.AuditActivityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditActivityRepository repository;

  public void record(
    String documentType,
    Long documentId,
    String activityType,
    String actor,
    String notes,
    String metadata
  ) {
    repository.save(
      AuditActivity.builder()
        .documentType(documentType)
        .documentId(documentId)
        .activityType(activityType)
        .actor(actor)
        .notes(notes)
        .metadata(metadata)
        .build()
    );
  }

  public List<AuditActivityResponse> getTimeline(String documentType, Long documentId) {
    return repository
      .findByDocumentTypeAndDocumentIdOrderByCreatedAtAsc(documentType, documentId)
      .stream()
      .map(AuditActivityResponse::from)
      .toList();
  }
}
