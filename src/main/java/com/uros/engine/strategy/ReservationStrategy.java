package com.uros.engine.strategy;

import com.uros.reservation.dto.AvailabilityRequest;
import com.uros.reservation.dto.AvailabilityResponse;
import com.uros.reservation.dto.ReservationRequest;
import com.uros.reservation.model.Reservation;

/**
 * Strategy interface for different reservation types.
 * Each reservation domain implements this to provide domain-specific logic.
 */
public interface ReservationStrategy {

    /**
     * Check resource availability based on domain-specific rules.
     */
    AvailabilityResponse checkAvailability(AvailabilityRequest request);

    /**
     * Create a temporary hold on the resource.
     */
    Reservation hold(ReservationRequest request, int holdDurationMinutes);

    /**
     * Release resources when a reservation is cancelled or expired.
     */
    void releaseResources(Reservation reservation);
}

