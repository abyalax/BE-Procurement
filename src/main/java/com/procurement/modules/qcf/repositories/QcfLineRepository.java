package com.procurement.modules.qcf.repositories;

import com.procurement.modules.qcf.entities.QcfLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcfLineRepository extends JpaRepository<QcfLine, Long> {
  List<QcfLine> findByQcfIdOrderByRankNo(Long qcfId);
}
