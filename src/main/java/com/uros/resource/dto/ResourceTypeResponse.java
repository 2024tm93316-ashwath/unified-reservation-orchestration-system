package com.uros.resource.dto;

import com.uros.common.enums.ReservationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceTypeResponse {
    private Long id;
    private String name;
    private String description;
    private ReservationType reservationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

