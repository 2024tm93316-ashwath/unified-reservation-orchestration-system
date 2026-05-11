package com.uros.dashboard.service;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
import com.uros.dashboard.dto.DashboardStats;
import com.uros.dashboard.dto.ResourceUtilization;
import com.uros.reservation.repository.BookingRepository;
import com.uros.reservation.repository.ReservationRepository;
import com.uros.resource.model.Resource;
import com.uros.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReservationRepository reservationRepository;
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;

    public DashboardStats getStats() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Map<String, Long> byType = new HashMap<>();
        for (ReservationType type : ReservationType.values()) {
            byType.put(type.name(), reservationRepository.countByReservationType(type));
        }

        return DashboardStats.builder()
                .totalReservations(reservationRepository.count())
                .activeHolds(reservationRepository.countByStatus(ReservationStatus.HELD))
                .confirmedBookings(reservationRepository.countByStatus(ReservationStatus.CONFIRMED))
                .cancelledBookings(reservationRepository.countByStatus(ReservationStatus.CANCELLED))
                .expiredReservations(reservationRepository.countByStatus(ReservationStatus.EXPIRED))
                .reservationsLast7Days(reservationRepository.countCreatedSince(sevenDaysAgo))
                .bookingsLast7Days(bookingRepository.countConfirmedSince(sevenDaysAgo))
                .reservationsByType(byType)
                .build();
    }

    public List<ResourceUtilization> getResourceUtilization() {
        List<Resource> resources = resourceRepository.findByIsActiveTrue();

        return resources.stream().map(resource -> {
            long active = reservationRepository.sumActiveQuantityByResource(resource.getId());
            int capacity = resource.getTotalCapacity() != null && resource.getTotalCapacity() > 0
                    ? resource.getTotalCapacity() : 1;
            double utilization = Math.min(((double) active / capacity) * 100.0, 100.0);

            return ResourceUtilization.builder()
                    .resourceId(resource.getId())
                    .resourceName(resource.getName())
                    .resourceTypeName(resource.getResourceType().getName())
                    .totalCapacity(resource.getTotalCapacity())
                    .activeReservations(active)
                    .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }
}

