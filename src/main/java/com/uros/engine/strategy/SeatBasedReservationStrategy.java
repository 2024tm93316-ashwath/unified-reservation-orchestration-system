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
import com.uros.resource.model.SeatMap;
import com.uros.resource.repository.SeatMapRepository;
import com.uros.resource.service.ResourceService;
import com.uros.resource.service.SeatMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for seat-based reservations (events/transport).
 * Booking specific identifiable seats from a seat map.
 */
@Component
@RequiredArgsConstructor
public class SeatBasedReservationStrategy implements ReservationStrategy {

    private final ReservationRepository reservationRepository;
    private final SeatMapRepository seatMapRepository;
    private final SeatMapService seatMapService;
    private final ResourceService resourceService;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        List<SeatMap> availableSeats = seatMapRepository.findByResourceIdAndIsAvailableTrue(request.getResourceId());

        List<AvailabilityResponse.AvailableSlot> slots = availableSeats.stream()
                .map(seat -> AvailabilityResponse.AvailableSlot.builder()
                        .slotId(seat.getId())
                        .label(seat.getSeatIdentifier() + " (Row:" + seat.getSeatRow() + " Col:" + seat.getSeatColumn() + ")")
                        .remainingCapacity(1)
                        .build())
                .collect(Collectors.toList());

        return AvailabilityResponse.builder()
                .available(!slots.isEmpty())
                .availableCount(slots.size())
                .availableSlots(slots)
                .message(slots.isEmpty() ? "No seats available" : slots.size() + " seat(s) available")
                .build();
    }

    @Override
    public Reservation hold(ReservationRequest request, int holdDurationMinutes) {
        if (request.getSeatMapId() == null) {
            throw new BadRequestException("Seat map ID is required for seat-based reservations");
        }

        Resource resource = resourceService.getEntity(request.getResourceId());

        // Pessimistic lock on the seat to prevent double-booking
        SeatMap seat = seatMapRepository.findByIdWithLock(request.getSeatMapId())
                .orElseThrow(() -> new BadRequestException("Seat not found with id: " + request.getSeatMapId()));

        if (!seat.getIsAvailable()) {
            throw new ConflictException("Seat " + seat.getSeatIdentifier() + " is already reserved");
        }

        // Also verify no active reservation exists for this seat
        long activeCount = reservationRepository.countActiveBySeatMap(seat.getId());
        if (activeCount > 0) {
            throw new ConflictException("Seat " + seat.getSeatIdentifier() + " is already reserved");
        }

        // Mark seat as unavailable
        seat.setIsAvailable(false);
        seatMapRepository.save(seat);

        com.uros.reservation.model.SeatBasedReservation reservation = new com.uros.reservation.model.SeatBasedReservation();
        reservation.setResource(resource);
        reservation.setUserId(request.getUserId());
        reservation.setReservationType(ReservationType.SEAT_BASED);
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setSeatMap(seat);
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes));

        return reservationRepository.save(reservation);
    }

    @Override
    public void releaseResources(Reservation reservation) {
        if (reservation instanceof com.uros.reservation.model.SeatBasedReservation) {
            SeatMap seat = seatMapRepository.findById(((com.uros.reservation.model.SeatBasedReservation) reservation).getSeatMap().getId()).orElse(null);
            if (seat != null) {
                seat.setIsAvailable(true);
                seatMapRepository.save(seat);
            }
        }
    }
}

