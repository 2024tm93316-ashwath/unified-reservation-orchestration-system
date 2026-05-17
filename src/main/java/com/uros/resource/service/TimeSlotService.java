package com.uros.resource.service;

import com.uros.common.exception.ResourceNotFoundException;
import com.uros.reservation.repository.ReservationRepository;
import com.uros.resource.dto.TimeSlotRequest;
import com.uros.resource.dto.TimeSlotResponse;
import com.uros.resource.model.Resource;
import com.uros.resource.model.TimeSlot;
import com.uros.resource.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository repository;
    private final ResourceService resourceService;
    private final ReservationRepository reservationRepository;

    @Transactional
    public TimeSlotResponse create(TimeSlotRequest request) {
        Resource resource = resourceService.getEntity(request.getResourceId());
        TimeSlot entity = TimeSlot.builder()
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxParallelCapacity(request.getMaxParallelCapacity() != null ? request.getMaxParallelCapacity() : 1)
                .isActive(true)
                .build();
        return toResponse(repository.save(entity));
    }

    public List<TimeSlotResponse> findByResourceId(Long resourceId) {
        return repository.findByResourceIdAndIsActiveTrue(resourceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TimeSlot getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot not found with id: " + id));
    }

    @Transactional
    public void delete(Long id) {
        TimeSlot entity = getEntity(id);
        entity.setIsActive(false);
        repository.save(entity);
    }

    private TimeSlotResponse toResponse(TimeSlot entity) {
        long currentBookings = reservationRepository.countActiveByTimeSlot(entity.getId());
        return TimeSlotResponse.builder()
                .id(entity.getId())
                .resourceId(entity.getResource().getId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .maxParallelCapacity(entity.getMaxParallelCapacity())
                .isActive(entity.getIsActive())
                .currentBookings(currentBookings)
                .build();
    }
}

