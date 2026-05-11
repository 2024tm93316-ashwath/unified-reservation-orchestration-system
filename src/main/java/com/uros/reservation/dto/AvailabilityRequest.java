package com.uros.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    // For TIME_BASED / RESOURCE_BASED
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // For SEAT_BASED
    private Long seatMapId;

    // For QUOTA_BASED
    private Long quotaDefinitionId;

    // For CAPACITY_BASED
    @Builder.Default
    private Integer quantity = 1;
}

