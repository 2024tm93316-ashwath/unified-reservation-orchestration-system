package com.uros.policy.service;

import com.uros.common.exception.ResourceNotFoundException;
import com.uros.policy.dto.PolicyRequest;
import com.uros.policy.dto.PolicyResponse;
import com.uros.policy.model.Policy;
import com.uros.policy.repository.PolicyRepository;
import com.uros.resource.model.ResourceType;
import com.uros.resource.service.ResourceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository repository;
    private final ResourceTypeService resourceTypeService;

    @Transactional
    public PolicyResponse create(PolicyRequest request) {
        ResourceType type = resourceTypeService.getEntity(request.getResourceTypeId());
        Policy entity = Policy.builder()
                .resourceType(type)
                .maxBookingsPerUser(request.getMaxBookingsPerUser() != null ? request.getMaxBookingsPerUser() : 5)
                .holdDurationMinutes(request.getHoldDurationMinutes() != null ? request.getHoldDurationMinutes() : 15)
                .maxAdvanceBookingDays(request.getMaxAdvanceBookingDays() != null ? request.getMaxAdvanceBookingDays() : 30)
                .allowOverlapping(request.getAllowOverlapping() != null ? request.getAllowOverlapping() : false)
                .description(request.getDescription())
                .build();
        return toResponse(repository.save(entity));
    }

    public List<PolicyResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PolicyResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public PolicyResponse update(Long id, PolicyRequest request) {
        Policy entity = getEntity(id);
        if (request.getMaxBookingsPerUser() != null) entity.setMaxBookingsPerUser(request.getMaxBookingsPerUser());
        if (request.getHoldDurationMinutes() != null) entity.setHoldDurationMinutes(request.getHoldDurationMinutes());
        if (request.getMaxAdvanceBookingDays() != null) entity.setMaxAdvanceBookingDays(request.getMaxAdvanceBookingDays());
        if (request.getAllowOverlapping() != null) entity.setAllowOverlapping(request.getAllowOverlapping());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private Policy getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
    }

    private PolicyResponse toResponse(Policy entity) {
        return PolicyResponse.builder()
                .id(entity.getId())
                .resourceTypeId(entity.getResourceType().getId())
                .resourceTypeName(entity.getResourceType().getName())
                .maxBookingsPerUser(entity.getMaxBookingsPerUser())
                .holdDurationMinutes(entity.getHoldDurationMinutes())
                .maxAdvanceBookingDays(entity.getMaxAdvanceBookingDays())
                .allowOverlapping(entity.getAllowOverlapping())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

