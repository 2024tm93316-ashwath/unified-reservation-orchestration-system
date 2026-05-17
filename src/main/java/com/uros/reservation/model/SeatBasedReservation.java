package com.uros.reservation.model;

import com.uros.resource.model.SeatMap;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seat_based_reservations")
@Getter
@Setter
public class SeatBasedReservation extends Reservation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_map_id", nullable = false)
    private SeatMap seatMap;
}
