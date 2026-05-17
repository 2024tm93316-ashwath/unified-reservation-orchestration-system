package com.uros.reservation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "capacity_based_reservations")
@Getter
@Setter
public class CapacityBasedReservation extends Reservation {

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
}
