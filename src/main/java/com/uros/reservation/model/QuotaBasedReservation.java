package com.uros.reservation.model;

import com.uros.resource.model.QuotaDefinition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quota_based_reservations")
@Getter
@Setter
public class QuotaBasedReservation extends Reservation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quota_definition_id", nullable = false)
    private QuotaDefinition quotaDefinition;
}
