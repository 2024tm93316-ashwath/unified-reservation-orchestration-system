package com.uros.engine.orchestrator;

import com.uros.common.enums.ReservationType;
import com.uros.common.exception.BadRequestException;
import com.uros.engine.strategy.*;
import com.uros.reservation.dto.AvailabilityRequest;
import com.uros.reservation.dto.AvailabilityResponse;
import com.uros.reservation.dto.ReservationRequest;
import com.uros.reservation.model.Reservation;
import com.uros.resource.model.Resource;
import com.uros.resource.service.ResourceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central orchestrator that routes reservation operations to the correct strategy
 * based on the resource's reservation type. This is the core of the engine.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationOrchestrator {

    private final ResourceService resourceService;
    private final TimeBasedReservationStrategy timeBasedStrategy;
    private final ResourceBasedReservationStrategy resourceBasedStrategy;
    private final SeatBasedReservationStrategy seatBasedStrategy;
    private final QuotaBasedReservationStrategy quotaBasedStrategy;
    private final CapacityBasedReservationStrategy capacityBasedStrategy;

    private final Map<ReservationType, ReservationStrategy> strategyMap = new EnumMap<>(ReservationType.class);

    @PostConstruct
    public void init() {
        strategyMap.put(ReservationType.TIME_BASED, timeBasedStrategy);
        strategyMap.put(ReservationType.RESOURCE_BASED, resourceBasedStrategy);
        strategyMap.put(ReservationType.SEAT_BASED, seatBasedStrategy);
        strategyMap.put(ReservationType.QUOTA_BASED, quotaBasedStrategy);
        strategyMap.put(ReservationType.CAPACITY_BASED, capacityBasedStrategy);
        log.info("ReservationOrchestrator initialized with {} strategies", strategyMap.size());
    }

    /**
     * Resolve the correct strategy for a given resource.
     */
    public ReservationStrategy resolveStrategy(Long resourceId) {
        Resource resource = resourceService.getEntity(resourceId);
        ReservationType type = resource.getResourceType().getReservationType();
        ReservationStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new BadRequestException("No strategy registered for reservation type: " + type);
        }
        return strategy;
    }

    /**
     * Check availability using the appropriate strategy.
     */
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        log.debug("Checking availability for resource {}", request.getResourceId());
        return resolveStrategy(request.getResourceId()).checkAvailability(request);
    }

    /**
     * Hold a resource using the appropriate strategy.
     */
    public Reservation hold(ReservationRequest request, int holdDurationMinutes) {
        log.debug("Creating hold for resource {} by user {}", request.getResourceId(), request.getUserId());
        return resolveStrategy(request.getResourceId()).hold(request, holdDurationMinutes);
    }

    /**
     * Release resources using the appropriate strategy.
     */
    public void releaseResources(Reservation reservation) {
        ReservationType type = reservation.getReservationType();
        ReservationStrategy strategy = strategyMap.get(type);
        if (strategy != null) {
            log.debug("Releasing resources for reservation {}", reservation.getId());
            strategy.releaseResources(reservation);
        }
    }
}

