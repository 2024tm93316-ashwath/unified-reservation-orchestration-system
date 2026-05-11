package com.uros.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponse {
    private Long id;
    private Long resourceId;
    private String seatIdentifier;
    private String seatRow;
    private String seatColumn;
    private Boolean isAvailable;
}

