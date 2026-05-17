package com.uros.reservation.model;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
import com.uros.common.model.BaseEntity;
import com.uros.resource.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_type", nullable = false)
    private ReservationType reservationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Version
    private Long version;
}

