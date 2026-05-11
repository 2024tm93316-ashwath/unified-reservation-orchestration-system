package com.uros.resource.service;

import com.uros.common.exception.ResourceNotFoundException;
import com.uros.resource.dto.QuotaDefinitionRequest;
import com.uros.resource.dto.QuotaDefinitionResponse;
import com.uros.resource.model.QuotaDefinition;
import com.uros.resource.model.Resource;
import com.uros.resource.repository.QuotaDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotaDefinitionService {

    private final QuotaDefinitionRepository repository;
    private final ResourceService resourceService;

    @Transactional
    public QuotaDefinitionResponse create(QuotaDefinitionRequest request) {
        Resource resource = resourceService.getEntity(request.getResourceId());
        QuotaDefinition entity = QuotaDefinition.builder()
                .resource(resource)
                .quotaName(request.getQuotaName())
                .maxAllocation(request.getMaxAllocation())
                .currentUsage(0)
                .build();
        return toResponse(repository.save(entity));
    }

    public List<QuotaDefinitionResponse> findByResourceId(Long resourceId) {
        return repository.findByResourceId(resourceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public QuotaDefinition getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuotaDefinition not found with id: " + id));
    }

    private QuotaDefinitionResponse toResponse(QuotaDefinition entity) {
        return QuotaDefinitionResponse.builder()
                .id(entity.getId())
                .resourceId(entity.getResource().getId())
                .quotaName(entity.getQuotaName())
                .maxAllocation(entity.getMaxAllocation())
                .currentUsage(entity.getCurrentUsage())
                .availableSlots(entity.getMaxAllocation() - entity.getCurrentUsage())
                .build();
    }
}

