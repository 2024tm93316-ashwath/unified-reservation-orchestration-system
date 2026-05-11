package com.uros.resource.service;

import com.uros.common.exception.ResourceNotFoundException;
import com.uros.resource.dto.ResourceRequest;
import com.uros.resource.dto.ResourceResponse;
import com.uros.resource.model.Resource;
import com.uros.resource.model.ResourceType;
import com.uros.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository repository;
    private final ResourceTypeService resourceTypeService;

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        ResourceType type = resourceTypeService.getEntity(request.getResourceTypeId());
        Resource entity = Resource.builder()
                .name(request.getName())
                .description(request.getDescription())
                .resourceType(type)
                .totalCapacity(request.getTotalCapacity())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(repository.save(entity));
    }

    public List<ResourceResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ResourceResponse> findActive() {
        return repository.findByIsActiveTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ResourceResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public ResourceResponse update(Long id, ResourceRequest request) {
        Resource entity = getEntity(id);
        ResourceType type = resourceTypeService.getEntity(request.getResourceTypeId());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setResourceType(type);
        entity.setTotalCapacity(request.getTotalCapacity());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    public Resource getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    private ResourceResponse toResponse(Resource entity) {
        return ResourceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .resourceTypeId(entity.getResourceType().getId())
                .resourceTypeName(entity.getResourceType().getName())
                .reservationType(entity.getResourceType().getReservationType())
                .totalCapacity(entity.getTotalCapacity())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

