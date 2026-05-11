package com.uros.resource.service;

import com.uros.common.exception.ResourceNotFoundException;
import com.uros.resource.dto.ResourceTypeRequest;
import com.uros.resource.dto.ResourceTypeResponse;
import com.uros.resource.model.ResourceType;
import com.uros.resource.repository.ResourceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceTypeService {

    private final ResourceTypeRepository repository;

    @Transactional
    public ResourceTypeResponse create(ResourceTypeRequest request) {
        ResourceType entity = ResourceType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .reservationType(request.getReservationType())
                .build();
        return toResponse(repository.save(entity));
    }

    public List<ResourceTypeResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ResourceTypeResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public ResourceTypeResponse update(Long id, ResourceTypeRequest request) {
        ResourceType entity = getEntity(id);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setReservationType(request.getReservationType());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    public ResourceType getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceType not found with id: " + id));
    }

    private ResourceTypeResponse toResponse(ResourceType entity) {
        return ResourceTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .reservationType(entity.getReservationType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

