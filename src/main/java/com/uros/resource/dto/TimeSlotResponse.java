package com.uros.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private Long resourceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParallelCapacity;
    private Boolean isActive;
    private Long currentBookings;
}

