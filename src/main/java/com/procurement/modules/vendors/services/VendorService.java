package com.procurement.modules.vendors.services;

import com.procurement.common.exception.BadRequestException;
import com.procurement.common.exception.ConflictException;
import com.procurement.common.exception.NotFoundException;
import com.procurement.common.pagination.PaginationSort;
import com.procurement.common.response.PageResponse;
import com.procurement.common.utils.SearchUtils;
import com.procurement.modules.vendors.dto.*;
import com.procurement.modules.vendors.entities.Vendor;
import com.procurement.modules.vendors.repositories.VendorRepository;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorService {

  private static final String ACTIVE = "ACTIVE";
  private static final String INACTIVE = "INACTIVE";

  private static final Map<String, String> SORT_FIELDS = Map.of(
    "id",
    "id",
    "name",
    "name",
    "email",
    "email",
    "status",
    "status",
    "performanceScore",
    "performanceScore",
    "createdAt",
    "createdAt",
    "updatedAt",
    "updatedAt"
  );

  private final VendorRepository repository;

  public PageResponse<VendorResponse> getVendors(
    int page,
    int limit,
    String sortBy,
    String sortOrder,
    String search,
    String status
  ) {
    Pageable pageable = PaginationSort.pageRequest(
      page,
      limit,
      sortBy,
      sortOrder,
      SORT_FIELDS,
      "name",
      Sort.Direction.ASC
    );
    String normalizedStatus = normalizeStatus(status);
    String normalizedSearch = SearchUtils.normalizeSearch(search);

    if (normalizedStatus != null && normalizedSearch != null) {
      return PageResponse.from(
        repository
          .searchVendorsByStatus(
            normalizedStatus,
            SearchUtils.toSearchPattern(normalizedSearch),
            pageable
          )
          .map(VendorResponse::from)
      );
    }

    if (normalizedStatus != null) {
      return PageResponse.from(
        repository.findByStatus(normalizedStatus, pageable).map(VendorResponse::from)
      );
    }

    if (normalizedSearch != null) {
      return PageResponse.from(
        repository
          .searchVendors(SearchUtils.toSearchPattern(normalizedSearch), pageable)
          .map(VendorResponse::from)
      );
    }

    return PageResponse.from(repository.findAll(pageable).map(VendorResponse::from));
  }

  public VendorResponse getVendorById(Long id) {
    return VendorResponse.from(requireVendor(id));
  }

  @Transactional
  public VendorResponse createVendor(CreateVendorRequest request) {
    String email = request.email().trim().toLowerCase();
    if (repository.existsByEmail(email)) throw new ConflictException(
      "Vendor email already registered"
    );
    Vendor vendor = repository.save(
      Vendor.builder()
        .name(request.name().trim())
        .email(email)
        .status(request.status() == null ? ACTIVE : normalizeStatus(request.status()))
        .performanceScore(
          request.performanceScore() == null ? BigDecimal.ZERO : request.performanceScore()
        )
        .build()
    );
    return VendorResponse.from(vendor);
  }

  @Transactional
  public VendorResponse updateVendor(Long id, UpdateVendorRequest request) {
    Vendor vendor = requireVendor(id);

    if (request.name() != null) vendor.setName(request.name().trim());

    if (request.email() != null) {
      String email = request.email().trim().toLowerCase();
      if (repository.existsByEmailAndIdNot(email, id)) throw new ConflictException(
        "Vendor email already registered"
      );
      vendor.setEmail(email);
    }

    if (request.performanceScore() != null) vendor.setPerformanceScore(request.performanceScore());

    if (request.status() != null) vendor.setStatus(normalizeStatus(request.status()));

    return VendorResponse.from(vendor);
  }

  @Transactional
  public void deactivateVendor(Long id) {
    Vendor vendor = requireVendor(id);
    vendor.setStatus("INACTIVE");
  }

  private Vendor requireVendor(Long id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException("Vendor not found"));
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) return null;

    String normalized = status.trim().toUpperCase();
    if (!ACTIVE.equals(normalized) && !INACTIVE.equals(normalized)) {
      throw new BadRequestException("Vendor status must be ACTIVE or INACTIVE");
    }
    return normalized;
  }
}
