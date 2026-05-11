package com.uros.resource.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.resource.dto.TimeSlotRequest;
import com.uros.resource.dto.TimeSlotResponse;
import com.uros.resource.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/time-slots")
@RequiredArgsConstructor
@Tag(name = "Time Slots", description = "Manage time slots for time-based resources")
public class TimeSlotController {

    private final TimeSlotService service;

    @PostMapping
    @Operation(summary = "Create a new time slot")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> create(@Valid @RequestBody TimeSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Time slot created", service.create(request)));
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "Get time slots by resource ID")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> findByResource(@PathVariable Long resourceId) {
        return ResponseEntity.ok(ApiResponse.success(service.findByResourceId(resourceId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a time slot")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Time slot deactivated", null));
    }
}

