package com.uros.engine.scheduler;

import com.uros.common.enums.ReservationStatus;
import com.uros.engine.orchestrator.ReservationOrchestrator;
import com.uros.reservation.model.Reservation;
import com.uros.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task that automatically expires unconfirmed held reservations.
 * Runs periodically to scan for reservations past their hold expiry time,
 * marks them as EXPIRED, and releases associated resources.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationOrchestrator orchestrator;

    @Scheduled(fixedDelayString = "${reservation.expiry-check-interval-ms:60000}")
    @Transactional
    public void expireHeldReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findExpiredHolds(now);

        if (!expiredReservations.isEmpty()) {
            log.info("Found {} expired held reservations to process", expiredReservations.size());
        }

        for (Reservation reservation : expiredReservations) {
            try {
                // Release resources via strategy
                orchestrator.releaseResources(reservation);

                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                log.info("Reservation {} expired and resources released", reservation.getId());
            } catch (Exception e) {
                log.error("Failed to expire reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }
    }
}

