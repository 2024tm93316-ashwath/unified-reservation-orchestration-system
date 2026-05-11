package com.uros.reservation.dto;

import com.uros.common.enums.ReservationStatus;
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
public class ReservationResponse {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private String userId;
    private ReservationType reservationType;
    private ReservationStatus status;
    private Long timeSlotId;
    private Long seatMapId;
    private Long quotaDefinitionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private LocalDateTime holdExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

