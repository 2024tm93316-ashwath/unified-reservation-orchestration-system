package com.uros.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalReservations;
    private long activeHolds;
    private long confirmedBookings;
    private long cancelledBookings;
    private long expiredReservations;
    private long reservationsLast7Days;
    private long bookingsLast7Days;
    private Map<String, Long> reservationsByType;
    private Map<String, Long> last7DaysCounts;
}

