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
import com.uros.resource.model.TimeSlot;
import com.uros.resource.repository.TimeSlotRepository;
import com.uros.resource.service.ResourceService;
import com.uros.resource.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for time-based reservations (salon/spa/appointment).
 * Booking based on predefined time slots with limited parallel capacity.
 */
@Component
@RequiredArgsConstructor
public class TimeBasedReservationStrategy implements ReservationStrategy {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotService timeSlotService;
    private final ResourceService resourceService;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        List<TimeSlot> slots = timeSlotRepository.findByResourceIdAndIsActiveTrue(request.getResourceId());
        List<AvailabilityResponse.AvailableSlot> availableSlots = new ArrayList<>();

        for (TimeSlot slot : slots) {
            long activeCount = reservationRepository.countActiveByTimeSlot(slot.getId());
            int remaining = slot.getMaxParallelCapacity() - (int) activeCount;
            if (remaining > 0) {
                availableSlots.add(AvailabilityResponse.AvailableSlot.builder()
                        .slotId(slot.getId())
                        .label(slot.getStartTime() + " - " + slot.getEndTime())
                        .remainingCapacity(remaining)
                        .build());
            }
        }

        return AvailabilityResponse.builder()
                .available(!availableSlots.isEmpty())
                .availableCount(availableSlots.size())
                .availableSlots(availableSlots)
                .message(availableSlots.isEmpty() ? "No available time slots" : availableSlots.size() + " slot(s) available")
                .build();
    }

    @Override
    public Reservation hold(ReservationRequest request, int holdDurationMinutes) {
        if (request.getTimeSlotId() == null) {
            throw new BadRequestException("Time slot ID is required for time-based reservations");
        }

        TimeSlot slot = timeSlotService.getEntity(request.getTimeSlotId());
        Resource resource = resourceService.getEntity(request.getResourceId());

        // Concurrency-safe: check availability inside transaction
        long activeCount = reservationRepository.countActiveByTimeSlot(slot.getId());
        if (activeCount >= slot.getMaxParallelCapacity()) {
            throw new ConflictException("Time slot is fully booked. No capacity remaining.");
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .userId(request.getUserId())
                .reservationType(ReservationType.TIME_BASED)
                .status(ReservationStatus.HELD)
                .timeSlot(slot)
                .holdExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes))
                .build();

        return reservationRepository.save(reservation);
    }

    @Override
    public void releaseResources(Reservation reservation) {
        // No additional cleanup needed for time-based; the slot becomes available
        // when the reservation status is no longer HELD/CONFIRMED
    }
}

