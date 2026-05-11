package com.uros.reservation.repository;

import com.uros.reservation.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByReservationId(Long reservationId);

    long countByCancelledAtIsNull();

    long countByCancelledAtIsNotNull();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.confirmedAt >= :since")
    long countConfirmedSince(@Param("since") LocalDateTime since);
}

