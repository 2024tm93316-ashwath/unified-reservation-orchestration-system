package com.uros.engine.strategy;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
import com.uros.common.exception.BadRequestException;
import com.uros.common.exception.ConflictException;
import com.uros.reservation.dto.AvailabilityRequest;
import com.uros.reservation.dto.AvailabilityResponse;
import com.uros.reservation.dto.ReservationRequest;
import com.uros.reservation.model.Reservation;
import com.uros.reservation.repository.ReservationRepository;
import com.uros.resource.model.Resource;
import com.uros.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Strategy for resource-based reservations (rooms/banquet halls).
 * Booking specific resources for time ranges with overlap detection.
 */
@Component
@RequiredArgsConstructor
public class ResourceBasedReservationStrategy implements ReservationStrategy {

    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("Start time and end time are required for resource-based availability check");
        }

        List<Reservation> overlapping = reservationRepository.findOverlapping(
                request.getResourceId(), request.getStartTime(), request.getEndTime());

        boolean available = overlapping.isEmpty();
        return AvailabilityResponse.builder()
                .available(available)
                .availableCount(available ? 1 : 0)
                .message(available ? "Resource is available for the requested time range"
                        : "Resource has conflicting reservations for the requested time range")
                .build();
    }

    @Override
    public Reservation hold(ReservationRequest request, int holdDurationMinutes) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("Start time and end time are required for resource-based reservations");
        }

        Resource resource = resourceService.getEntity(request.getResourceId());

        // Conflict detection
        List<Reservation> overlapping = reservationRepository.findOverlapping(
                request.getResourceId(), request.getStartTime(), request.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new ConflictException("Resource has conflicting reservations for the requested time range");
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .userId(request.getUserId())
                .reservationType(ReservationType.RESOURCE_BASED)
                .status(ReservationStatus.HELD)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .holdExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes))
                .build();

        return reservationRepository.save(reservation);
    }

    @Override
    public void releaseResources(Reservation reservation) {
        // No additional cleanup; overlap queries automatically exclude non-active statuses
    }
}

