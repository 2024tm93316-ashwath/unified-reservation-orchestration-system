package com.uros.resource.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.resource.dto.QuotaDefinitionRequest;
import com.uros.resource.dto.QuotaDefinitionResponse;
import com.uros.resource.service.QuotaDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotas")
@RequiredArgsConstructor
@Tag(name = "Quota Definitions", description = "Manage quota allocations for quota-based resources")
public class QuotaDefinitionController {

    private final QuotaDefinitionService service;

    @PostMapping
    @Operation(summary = "Create a quota definition")
    public ResponseEntity<ApiResponse<QuotaDefinitionResponse>> create(@Valid @RequestBody QuotaDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quota created", service.create(request)));
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "Get quotas for a resource")
    public ResponseEntity<ApiResponse<List<QuotaDefinitionResponse>>> findByResource(@PathVariable Long resourceId) {
        return ResponseEntity.ok(ApiResponse.success(service.findByResourceId(resourceId)));
    }
}

