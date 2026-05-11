package com.uros.engine;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
import com.uros.reservation.dto.ReservationRequest;
import com.uros.reservation.dto.ReservationResponse;
import com.uros.reservation.service.ReservationService;
import com.uros.resource.dto.*;
import com.uros.resource.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests to verify that the reservation engine prevents double-booking
 * when multiple users attempt to reserve the same resource simultaneously.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ResourceTypeService resourceTypeService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private SeatMapService seatMapService;

    @Autowired
    private QuotaDefinitionService quotaDefinitionService;

    @Autowired
    private TimeSlotService timeSlotService;

    private Long capacityResourceId;
    private Long seatResourceId;
    private Long seatMapId;

    @BeforeEach
    void setUp() {
        // Create a CAPACITY_BASED resource type and resource with capacity of 5
        ResourceTypeResponse capacityType = resourceTypeService.create(
                ResourceTypeRequest.builder()
                        .name("General Admission " + System.nanoTime())
                        .reservationType(ReservationType.CAPACITY_BASED)
                        .description("Test capacity type")
                        .build());

        ResourceResponse capacityResource = resourceService.create(
                ResourceRequest.builder()
                        .name("Test Venue " + System.nanoTime())
                        .resourceTypeId(capacityType.getId())
                        .totalCapacity(5)
                        .build());
        capacityResourceId = capacityResource.getId();

        // Create a SEAT_BASED resource with a single seat
        ResourceTypeResponse seatType = resourceTypeService.create(
                ResourceTypeRequest.builder()
                        .name("Event Seating " + System.nanoTime())
                        .reservationType(ReservationType.SEAT_BASED)
                        .description("Test seat type")
                        .build());

        ResourceResponse seatResource = resourceService.create(
                ResourceRequest.builder()
                        .name("Test Event " + System.nanoTime())
                        .resourceTypeId(seatType.getId())
                        .totalCapacity(1)
                        .build());
        seatResourceId = seatResource.getId();

        SeatMapResponse seat = seatMapService.create(
                SeatMapRequest.builder()
                        .resourceId(seatResourceId)
                        .seatIdentifier("A1")
                        .seatRow("A")
                        .seatColumn("1")
                        .build());
        seatMapId = seat.getId();
    }

    @Test
    @DisplayName("Capacity-based: Only 5 of 10 concurrent requests should succeed for capacity=5")
    void shouldLimitCapacityBasedReservations() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    ReservationRequest request = ReservationRequest.builder()
                            .resourceId(capacityResourceId)
                            .userId("user-" + userId)
                            .quantity(1)
                            .build();
                    ReservationResponse response = reservationService.createReservation(request);
                    if (response.getStatus() == ReservationStatus.HELD) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    errors.add(e.getMessage());
                }
            });
        }

        latch.countDown(); // Fire all threads simultaneously
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        System.out.println("Capacity test — Success: " + successCount.get() + ", Failed: " + failCount.get());
        assertTrue(successCount.get() <= 5, "Should not exceed capacity of 5, but got: " + successCount.get());
        assertTrue(successCount.get() >= 1, "At least 1 reservation should succeed");
    }

    @Test
    @DisplayName("Seat-based: Only 1 of 10 concurrent requests should succeed for a single seat")
    void shouldPreventDoubleSeatBooking() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    ReservationRequest request = ReservationRequest.builder()
                            .resourceId(seatResourceId)
                            .userId("seat-user-" + userId)
                            .seatMapId(seatMapId)
                            .build();
                    reservationService.createReservation(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        System.out.println("Seat test — Success: " + successCount.get() + ", Failed: " + failCount.get());
        assertEquals(1, successCount.get(), "Exactly 1 seat reservation should succeed");
    }
}

