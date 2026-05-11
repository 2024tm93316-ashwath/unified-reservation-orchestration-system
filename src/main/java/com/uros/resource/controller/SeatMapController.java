package com.uros.resource.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.resource.dto.SeatMapRequest;
import com.uros.resource.dto.SeatMapResponse;
import com.uros.resource.service.SeatMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seat-maps")
@RequiredArgsConstructor
@Tag(name = "Seat Maps", description = "Manage seat layouts for seat-based resources")
public class SeatMapController {

    private final SeatMapService service;

    @PostMapping
    @Operation(summary = "Create a seat map entry")
    public ResponseEntity<ApiResponse<SeatMapResponse>> create(@Valid @RequestBody SeatMapRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seat created", service.create(request)));
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "Get all seats for a resource")
    public ResponseEntity<ApiResponse<List<SeatMapResponse>>> findByResource(@PathVariable Long resourceId) {
        return ResponseEntity.ok(ApiResponse.success(service.findByResourceId(resourceId)));
    }

    @GetMapping("/resource/{resourceId}/available")
    @Operation(summary = "Get available seats for a resource")
    public ResponseEntity<ApiResponse<List<SeatMapResponse>>> findAvailable(@PathVariable Long resourceId) {
        return ResponseEntity.ok(ApiResponse.success(service.findAvailableByResourceId(resourceId)));
    }
}

