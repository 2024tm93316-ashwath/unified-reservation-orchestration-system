package com.uros.resource.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.resource.dto.ResourceRequest;
import com.uros.resource.dto.ResourceResponse;
import com.uros.resource.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Manage reservable resources")
public class ResourceController {

    private final ResourceService service;

    @PostMapping
    @Operation(summary = "Register a new resource")
    public ResponseEntity<ApiResponse<ResourceResponse>> create(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created", service.create(request)));
    }

    @GetMapping
    @Operation(summary = "Get all resources")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<ResourceResponse> resources = activeOnly ? service.findActive() : service.findAll();
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID")
    public ResponseEntity<ApiResponse<ResourceResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a resource")
    public ResponseEntity<ApiResponse<ResourceResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Resource updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Resource deleted", null));
    }
}

