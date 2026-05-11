package com.uros.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaDefinitionResponse {
    private Long id;
    private Long resourceId;
    private String quotaName;
    private Integer maxAllocation;
    private Integer currentUsage;
    private Integer availableSlots;
}

