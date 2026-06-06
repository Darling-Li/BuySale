package com.rice.trade.controller;

import com.rice.trade.dto.MonthlyTrendResponse;
import com.rice.trade.service.DashboardService;
import java.time.Year;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/monthly-trend")
    public MonthlyTrendResponse monthlyTrend(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String productType
    ) {
        int targetYear = year == null ? Year.now().getValue() : year;
        return dashboardService.monthlyTrend(targetYear, productType);
    }
}
