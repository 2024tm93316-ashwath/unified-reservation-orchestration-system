package com.uros.resource.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.resource.dto.ResourceTypeRequest;
import com.uros.resource.dto.ResourceTypeResponse;
import com.uros.resource.service.ResourceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resource-types")
@RequiredArgsConstructor
@Tag(name = "Resource Types", description = "Manage resource type definitions")
public class ResourceTypeController {

    private final ResourceTypeService service;

    @PostMapping
    @Operation(summary = "Create a new resource type")
    public ResponseEntity<ApiResponse<ResourceTypeResponse>> create(@Valid @RequestBody ResourceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource type created", service.create(request)));
    }

    @GetMapping
    @Operation(summary = "Get all resource types")
    public ResponseEntity<ApiResponse<List<ResourceTypeResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource type by ID")
    public ResponseEntity<ApiResponse<ResourceTypeResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a resource type")
    public ResponseEntity<ApiResponse<ResourceTypeResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody ResourceTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Resource type updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resource type")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Resource type deleted", null));
    }
}

