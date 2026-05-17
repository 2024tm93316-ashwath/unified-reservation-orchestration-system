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
    private final com.uros.policy.repository.PolicyRepository policyRepository;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("Start time and end time are required for resource-based availability check");
        }

        Resource resource = resourceService.getEntity(request.getResourceId());
        boolean allowOverlapping = false;
        com.uros.policy.model.Policy policy = policyRepository.findByResourceTypeId(resource.getResourceType().getId()).orElse(null);
        if (policy != null && Boolean.TRUE.equals(policy.getAllowOverlapping())) {
            allowOverlapping = true;
        }

        boolean available = true;
        if (!allowOverlapping) {
            List<Reservation> overlapping = reservationRepository.findOverlapping(
                    request.getResourceId(), request.getStartTime(), request.getEndTime());
            available = overlapping.isEmpty();
        }

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

        // Check policies
        boolean allowOverlapping = false;
        com.uros.policy.model.Policy policy = policyRepository.findByResourceTypeId(resource.getResourceType().getId()).orElse(null);
        if (policy != null) {
            if (Boolean.TRUE.equals(policy.getAllowOverlapping())) {
                allowOverlapping = true;
            }
            if (policy.getMaxAdvanceBookingDays() != null) {
                LocalDateTime maxAllowedDate = LocalDateTime.now().plusDays(policy.getMaxAdvanceBookingDays());
                if (request.getStartTime() != null && request.getStartTime().isAfter(maxAllowedDate)) {
                    throw new BadRequestException("Cannot book more than " + policy.getMaxAdvanceBookingDays() + " days in advance. Max allowed date is: " + maxAllowedDate.toLocalDate());
                }
            }
        }

        if (!allowOverlapping) {
            // Conflict detection
            List<Reservation> overlapping = reservationRepository.findOverlapping(
                    request.getResourceId(), request.getStartTime(), request.getEndTime());

            if (!overlapping.isEmpty()) {
                throw new ConflictException("Resource has conflicting reservations for the requested time range");
            }
        }

        com.uros.reservation.model.ResourceBasedReservation reservation = new com.uros.reservation.model.ResourceBasedReservation();
        reservation.setResource(resource);
        reservation.setUserId(request.getUserId());
        reservation.setReservationType(ReservationType.RESOURCE_BASED);
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes));

        return reservationRepository.save(reservation);
    }

    @Override
    public void releaseResources(Reservation reservation) {
        // No additional cleanup; overlap queries automatically exclude non-active statuses
    }
}

