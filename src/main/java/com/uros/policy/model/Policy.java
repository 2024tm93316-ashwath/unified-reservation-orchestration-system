package com.uros.policy.model;

import com.uros.common.model.BaseEntity;
import com.uros.resource.model.ResourceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @Column(name = "max_bookings_per_user")
    @Builder.Default
    private Integer maxBookingsPerUser = 5;

    @Column(name = "hold_duration_minutes")
    @Builder.Default
    private Integer holdDurationMinutes = 15;

    @Column(name = "max_advance_booking_days")
    @Builder.Default
    private Integer maxAdvanceBookingDays = 30;

    @Column(name = "allow_overlapping")
    @Builder.Default
    private Boolean allowOverlapping = false;

    private String description;
}

