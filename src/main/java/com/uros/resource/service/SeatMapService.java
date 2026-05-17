package com.uros.resource.service;

import com.uros.common.exception.ResourceNotFoundException;
import com.uros.resource.dto.SeatMapRequest;
import com.uros.resource.dto.SeatMapResponse;
import com.uros.resource.model.Resource;
import com.uros.resource.model.SeatMap;
import com.uros.resource.repository.SeatMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatMapService {

    private final SeatMapRepository repository;
    private final ResourceService resourceService;
    private final com.uros.reservation.repository.ReservationRepository reservationRepository;

    @Transactional
    public SeatMapResponse create(SeatMapRequest request) {
        Resource resource = resourceService.getEntity(request.getResourceId());
        SeatMap entity = SeatMap.builder()
                .resource(resource)
                .seatIdentifier(request.getSeatIdentifier())
                .seatRow(request.getSeatRow())
                .seatColumn(request.getSeatColumn())
                .isAvailable(true)
                .build();
        return toResponse(repository.save(entity));
    }

    public List<SeatMapResponse> findByResourceId(Long resourceId) {
        return repository.findByResourceId(resourceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SeatMapResponse> findAvailableByResourceId(Long resourceId) {
        return repository.findByResourceIdAndIsAvailableTrue(resourceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SeatMap getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SeatMap not found with id: " + id));
    }

    private SeatMapResponse toResponse(SeatMap entity) {
        boolean isBooked = reservationRepository.countActiveBySeatMap(entity.getId()) > 0;
        return SeatMapResponse.builder()
                .id(entity.getId())
                .resourceId(entity.getResource().getId())
                .seatIdentifier(entity.getSeatIdentifier())
                .seatRow(entity.getSeatRow())
                .seatColumn(entity.getSeatColumn())
                .isAvailable(entity.getIsAvailable() && !isBooked)
                .build();
    }
}

