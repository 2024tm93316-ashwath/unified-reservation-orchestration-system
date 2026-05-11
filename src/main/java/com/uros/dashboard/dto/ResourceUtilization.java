package com.uros.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUtilization {
    private Long resourceId;
    private String resourceName;
    private String resourceTypeName;
    private Integer totalCapacity;
    private long activeReservations;
    private double utilizationPercentage;
}

