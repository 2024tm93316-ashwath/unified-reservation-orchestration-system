package com.uros.policy.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.policy.dto.PolicyRequest;
import com.uros.policy.dto.PolicyResponse;
import com.uros.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Administration — manage reservation policies")
public class PolicyController {

    private final PolicyService service;

    @PostMapping
    @Operation(summary = "Create a reservation policy")
    public ResponseEntity<ApiResponse<PolicyResponse>> create(@Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Policy created", service.create(request)));
    }

    @GetMapping
    @Operation(summary = "Get all policies")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<ApiResponse<PolicyResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a policy")
    public ResponseEntity<ApiResponse<PolicyResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Policy updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a policy")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Policy deleted", null));
    }
}

