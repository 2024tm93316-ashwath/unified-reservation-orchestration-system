package com.uros.policy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRequest {

    @NotNull(message = "Resource type ID is required")
    private Long resourceTypeId;

    @Positive(message = "Max bookings per user must be positive")
    private Integer maxBookingsPerUser;

    @Positive(message = "Hold duration must be positive")
    private Integer holdDurationMinutes;

    @Positive(message = "Max advance booking days must be positive")
    private Integer maxAdvanceBookingDays;

    private Boolean allowOverlapping;

    private String description;
}

