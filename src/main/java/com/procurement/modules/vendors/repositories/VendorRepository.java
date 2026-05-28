package com.procurement.modules.vendors.repositories;

import com.procurement.modules.vendors.entities.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
  boolean existsByEmail(String email);
  boolean existsByEmailAndIdNot(String email, Long id);
  long countByStatus(String status);
  Page<Vendor> findByStatus(String status, Pageable pageable);

  @Query(
    """
    SELECT v
    FROM Vendor v
    WHERE LOWER(v.name) LIKE :search
       OR LOWER(v.email) LIKE :search
    """
  )
  Page<Vendor> searchVendors(@Param("search") String search, Pageable pageable);

  @Query(
    """
    SELECT v
    FROM Vendor v
    WHERE v.status = :status
      AND (
        LOWER(v.name) LIKE :search
        OR LOWER(v.email) LIKE :search
      )
    """
  )
  Page<Vendor> searchVendorsByStatus(
    @Param("status") String status,
    @Param("search") String search,
    Pageable pageable
  );
}
