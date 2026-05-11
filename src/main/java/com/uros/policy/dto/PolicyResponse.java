package com.uros.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Long id;
    private Long resourceTypeId;
    private String resourceTypeName;
    private Integer maxBookingsPerUser;
    private Integer holdDurationMinutes;
    private Integer maxAdvanceBookingDays;
    private Boolean allowOverlapping;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

