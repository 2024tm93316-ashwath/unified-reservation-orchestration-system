package com.uros.reservation.model;

import com.uros.resource.model.TimeSlot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "time_based_reservations")
@Getter
@Setter
public class TimeBasedReservation extends Reservation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;
}
