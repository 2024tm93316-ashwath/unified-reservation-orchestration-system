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

        Map<String, Long> last7DaysCounts = new java.util.LinkedHashMap<>();
        // Initialize with zeros for the last 7 days
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            last7DaysCounts.put(LocalDateTime.now().minusDays(i).format(formatter), 0L);
        }

        // Group actual reservations by day
        List<com.uros.reservation.model.Reservation> recentReservations = reservationRepository.findCreatedSince(sevenDaysAgo);
        for (com.uros.reservation.model.Reservation r : recentReservations) {
            String day = r.getCreatedAt().format(formatter);
            if (last7DaysCounts.containsKey(day)) {
                last7DaysCounts.put(day, last7DaysCounts.get(day) + 1);
            }
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
                .last7DaysCounts(last7DaysCounts)
                .build();
    }

    public List<ResourceUtilization> getResourceUtilization() {
        List<Resource> resources = resourceRepository.findByIsActiveTrue();

        return resources.stream().map(resource -> {
            ReservationType resType = resource.getResourceType().getReservationType();

            // CAPACITY_BASED sums quantity; RESOURCE_BASED only counts future/ongoing;
            // all other types count individual active reservations
            long active;
            if (resType == ReservationType.CAPACITY_BASED) {
                active = reservationRepository.sumActiveQuantityByResource(resource.getId());
            } else if (resType == ReservationType.RESOURCE_BASED) {
                active = reservationRepository.countFutureActiveByResource(resource.getId(), LocalDateTime.now());
            } else {
                active = reservationRepository.countActiveByResource(resource.getId());
            }

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

