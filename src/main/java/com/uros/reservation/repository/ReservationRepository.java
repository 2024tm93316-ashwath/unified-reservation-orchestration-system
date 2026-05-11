package com.uros.reservation.repository;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
import com.uros.reservation.model.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(String userId);

    List<Reservation> findByResourceIdAndStatus(Long resourceId, ReservationStatus status);

    // Count active reservations for a time slot
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.timeSlot.id = :timeSlotId " +
           "AND r.status IN ('HELD', 'CONFIRMED')")
    long countActiveByTimeSlot(@Param("timeSlotId") Long timeSlotId);

    // Find overlapping resource-based reservations
    @Query("SELECT r FROM Reservation r WHERE r.resource.id = :resourceId " +
           "AND r.status IN ('HELD', 'CONFIRMED') " +
           "AND r.startTime < :endTime AND r.endTime > :startTime")
    List<Reservation> findOverlapping(@Param("resourceId") Long resourceId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    // Count active reservations for capacity-based
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Reservation r WHERE r.resource.id = :resourceId " +
           "AND r.status IN ('HELD', 'CONFIRMED')")
    long sumActiveQuantityByResource(@Param("resourceId") Long resourceId);

    // Find expired held reservations
    @Query("SELECT r FROM Reservation r WHERE r.status = 'HELD' AND r.holdExpiresAt < :now")
    List<Reservation> findExpiredHolds(@Param("now") LocalDateTime now);

    // Count active bookings per user per resource type
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId " +
           "AND r.resource.resourceType.id = :resourceTypeId " +
           "AND r.status IN ('HELD', 'CONFIRMED')")
    long countActiveByUserAndResourceType(@Param("userId") String userId,
                                          @Param("resourceTypeId") Long resourceTypeId);

    // For seat-based: check if seat is reserved
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.seatMap.id = :seatMapId " +
           "AND r.status IN ('HELD', 'CONFIRMED')")
    long countActiveBySeatMap(@Param("seatMapId") Long seatMapId);

    // For quota-based: count active by quota
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.quotaDefinition.id = :quotaId " +
           "AND r.status IN ('HELD', 'CONFIRMED')")
    long countActiveByQuota(@Param("quotaId") Long quotaId);

    // Dashboard queries
    long countByStatus(ReservationStatus status);

    long countByReservationType(ReservationType type);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.createdAt >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    java.util.Optional<Reservation> findByIdWithLock(@Param("id") Long id);
}

