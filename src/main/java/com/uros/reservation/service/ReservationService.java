package com.uros.reservation.service;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.exception.BadRequestException;
import com.uros.common.exception.ConflictException;
import com.uros.common.exception.ResourceNotFoundException;
import com.uros.engine.orchestrator.ReservationOrchestrator;
import com.uros.policy.model.Policy;
import com.uros.policy.repository.PolicyRepository;
import com.uros.reservation.dto.*;
import com.uros.reservation.model.Booking;
import com.uros.reservation.model.Reservation;
import com.uros.reservation.repository.BookingRepository;
import com.uros.reservation.repository.ReservationRepository;
import com.uros.resource.model.Resource;
import com.uros.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookingRepository bookingRepository;
    private final ReservationOrchestrator orchestrator;
    private final ResourceService resourceService;
    private final PolicyRepository policyRepository;

    @Value("${reservation.default-hold-duration-minutes:15}")
    private int defaultHoldDurationMinutes;

    /**
     * Check availability for a resource.
     */
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        return orchestrator.checkAvailability(request);
    }

    /**
     * Create a reservation (temporary hold).
     */
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Resource resource = resourceService.getEntity(request.getResourceId());

        // Check policy limits
        int holdDuration = defaultHoldDurationMinutes;
        Policy policy = policyRepository.findByResourceTypeId(resource.getResourceType().getId()).orElse(null);
        if (policy != null) {
            holdDuration = policy.getHoldDurationMinutes();

            // Check per-reservation quantity limit
            int incomingQuantity = request.getQuantity() != null ? request.getQuantity() : 1;
            
            if (incomingQuantity > policy.getMaxBookingsPerUser()) {
                throw new com.uros.common.exception.ConflictException("Booking quantity limit exceeded. Policy allows max " + 
                        policy.getMaxBookingsPerUser() + " per reservation. You requested " + incomingQuantity + ".");
            }
        }

        // Delegate to orchestrator (strategy pattern)
        Reservation reservation = orchestrator.hold(request, holdDuration);
        log.info("Reservation {} created with HELD status, expires at {}",
                reservation.getId(), reservation.getHoldExpiresAt());

        return toResponse(reservation);
    }

    /**
     * Confirm a reservation into a final booking.
     */
    @Transactional
    public BookingResponse confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.HELD) {
            throw new BadRequestException("Only HELD reservations can be confirmed. Current status: " + reservation.getStatus());
        }

        // Check if hold has expired
        if (reservation.getHoldExpiresAt() != null && reservation.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            orchestrator.releaseResources(reservation);
            throw new ConflictException("Reservation has expired and cannot be confirmed");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        Booking booking = Booking.builder()
                .reservation(reservation)
                .confirmedAt(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        log.info("Reservation {} confirmed as booking {}", reservationId, booking.getId());

        return BookingResponse.builder()
                .id(booking.getId())
                .reservationId(reservation.getId())
                .reservation(toResponse(reservation))
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }

    /**
     * Cancel a reservation or booking.
     */
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
            reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new BadRequestException("Reservation is already " + reservation.getStatus());
        }

        // Release resources via orchestrator
        orchestrator.releaseResources(reservation);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // If there's a booking, mark it as cancelled
        bookingRepository.findByReservationId(reservationId).ifPresent(booking -> {
            booking.setCancelledAt(LocalDateTime.now());
            bookingRepository.save(booking);
        });

        log.info("Reservation {} cancelled", reservationId);
        return toResponse(reservation);
    }

    /**
     * Get reservation by ID.
     */
    public ReservationResponse getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        return toResponse(reservation);
    }

    /**
     * Get all reservations for a user.
     */
    public List<ReservationResponse> getUserReservations(String userId) {
        return reservationRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get all reservations.
     */
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse.ReservationResponseBuilder builder = ReservationResponse.builder()
                .id(r.getId())
                .resourceId(r.getResource().getId())
                .resourceName(r.getResource().getName())
                .userId(r.getUserId())
                .reservationType(r.getReservationType())
                .status(r.getStatus())
                .holdExpiresAt(r.getHoldExpiresAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt());

        if (r instanceof com.uros.reservation.model.TimeBasedReservation) {
            builder.timeSlotId(((com.uros.reservation.model.TimeBasedReservation) r).getTimeSlot() != null ? ((com.uros.reservation.model.TimeBasedReservation) r).getTimeSlot().getId() : null);
        } else if (r instanceof com.uros.reservation.model.SeatBasedReservation) {
            builder.seatMapId(((com.uros.reservation.model.SeatBasedReservation) r).getSeatMap() != null ? ((com.uros.reservation.model.SeatBasedReservation) r).getSeatMap().getId() : null);
        } else if (r instanceof com.uros.reservation.model.QuotaBasedReservation) {
            builder.quotaDefinitionId(((com.uros.reservation.model.QuotaBasedReservation) r).getQuotaDefinition() != null ? ((com.uros.reservation.model.QuotaBasedReservation) r).getQuotaDefinition().getId() : null);
        } else if (r instanceof com.uros.reservation.model.ResourceBasedReservation) {
            builder.startTime(((com.uros.reservation.model.ResourceBasedReservation) r).getStartTime())
                   .endTime(((com.uros.reservation.model.ResourceBasedReservation) r).getEndTime());
        } else if (r instanceof com.uros.reservation.model.CapacityBasedReservation) {
            builder.quantity(((com.uros.reservation.model.CapacityBasedReservation) r).getQuantity());
        }

        return builder.build();
    }
}

