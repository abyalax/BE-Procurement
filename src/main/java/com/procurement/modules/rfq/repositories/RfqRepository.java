package com.procurement.modules.rfq.repositories;

import com.procurement.modules.rfq.entities.Rfq;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RfqRepository extends JpaRepository<Rfq, Long> {
  long countByStatus(String status);

  @Query(
    """
    select r.id
    from Rfq r
    where r.status = 'ACTIVE'
      and r.deadline <= :now
    order by r.deadline asc, r.id asc
    """
  )
  List<Long> findExpiredActiveIds(@Param("now") LocalDateTime now, Pageable pageable);
}
