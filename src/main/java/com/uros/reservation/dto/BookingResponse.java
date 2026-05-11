package com.uros.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long reservationId;
    private ReservationResponse reservation;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
}

