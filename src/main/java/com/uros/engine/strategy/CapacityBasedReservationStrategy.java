package com.uros.engine.strategy;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
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

/**
 * Strategy for capacity-based reservations (general admission/bulk booking).
 * Booking based on total capacity without individual unit selection.
 */
@Component
@RequiredArgsConstructor
public class CapacityBasedReservationStrategy implements ReservationStrategy {

    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        Resource resource = resourceService.getEntity(request.getResourceId());
        int totalCapacity = resource.getTotalCapacity() != null ? resource.getTotalCapacity() : 0;
        long usedCapacity = reservationRepository.sumActiveQuantityByResource(request.getResourceId());
        int available = totalCapacity - (int) usedCapacity;
        int requestedQty = request.getQuantity() != null ? request.getQuantity() : 1;

        return AvailabilityResponse.builder()
                .available(available >= requestedQty)
                .availableCount(Math.max(available, 0))
                .message(available >= requestedQty
                        ? available + " unit(s) available out of " + totalCapacity
                        : "Insufficient capacity. Available: " + available + ", Requested: " + requestedQty)
                .build();
    }

    @Override
    public Reservation hold(ReservationRequest request, int holdDurationMinutes) {
        Resource resource = resourceService.getEntity(request.getResourceId());
        int totalCapacity = resource.getTotalCapacity() != null ? resource.getTotalCapacity() : 0;
        int requestedQty = request.getQuantity() != null ? request.getQuantity() : 1;

        long usedCapacity = reservationRepository.sumActiveQuantityByResource(request.getResourceId());
        int available = totalCapacity - (int) usedCapacity;

        if (available < requestedQty) {
            throw new ConflictException("Insufficient capacity. Available: " + available + ", Requested: " + requestedQty);
        }

        com.uros.reservation.model.CapacityBasedReservation reservation = new com.uros.reservation.model.CapacityBasedReservation();
        reservation.setResource(resource);
        reservation.setUserId(request.getUserId());
        reservation.setReservationType(ReservationType.CAPACITY_BASED);
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setQuantity(requestedQty);
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes));

        return reservationRepository.save(reservation);
    }

    @Override
    public void releaseResources(Reservation reservation) {
        // Capacity is automatically recalculated from active reservation counts
    }
}

