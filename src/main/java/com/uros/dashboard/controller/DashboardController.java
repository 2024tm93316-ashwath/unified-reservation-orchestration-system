package com.uros.dashboard.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.dashboard.dto.DashboardStats;
import com.uros.dashboard.dto.ResourceUtilization;
import com.uros.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Monitoring and analytics")
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/stats")
    @Operation(summary = "Get reservation statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(service.getStats()));
    }

    @GetMapping("/utilization")
    @Operation(summary = "Get resource utilization data")
    public ResponseEntity<ApiResponse<List<ResourceUtilization>>> getUtilization() {
        return ResponseEntity.ok(ApiResponse.success(service.getResourceUtilization()));
    }
}

