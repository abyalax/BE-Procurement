package com.procurement.modules.dashboard.controller;

import com.procurement.common.response.ApiResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.dashboard.dto.ProcurementSummaryResponse;
import com.procurement.modules.dashboard.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService service;

  @GetMapping("/procurement-summary")
  @Guard
  public ApiResponse<ProcurementSummaryResponse> procurementSummary() {
    return ApiResponse.success(
      "Procurement summary fetched successfully",
      service.procurementSummary()
    );
  }
}
