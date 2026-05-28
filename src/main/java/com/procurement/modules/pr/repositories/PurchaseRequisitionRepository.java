package com.procurement.modules.pr.repositories;

import com.procurement.modules.pr.entities.PurchaseRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {
  long countByStatus(String status);
  Page<PurchaseRequisition> findByStatus(String status, Pageable pageable);

  @Query(
    """
    SELECT pr
    FROM PurchaseRequisition pr
    WHERE LOWER(pr.title) LIKE :search
       OR LOWER(pr.description) LIKE :search
    """
  )
  Page<PurchaseRequisition> searchPurchaseRequisition(
    @Param("search") String search,
    Pageable pageable
  );

  @Query(
    """
    SELECT pr
    FROM PurchaseRequisition pr
    WHERE pr.status = :status
      AND (
        LOWER(pr.title) LIKE :search
        OR LOWER(pr.description) LIKE :search
      )
    """
  )
  Page<PurchaseRequisition> searchPurchaseRequisitionByStatus(
    @Param("status") String status,
    @Param("search") String search,
    Pageable pageable
  );
}
