package com.uros.engine.strategy;

import com.uros.common.enums.ReservationStatus;
import com.uros.common.enums.ReservationType;
import com.uros.common.exception.BadRequestException;
import com.uros.common.exception.ConflictException;
import com.uros.reservation.dto.AvailabilityRequest;
import com.uros.reservation.dto.AvailabilityResponse;
import com.uros.reservation.dto.ReservationRequest;
import com.uros.reservation.model.Reservation;
import com.uros.reservation.repository.ReservationRepository;
import com.uros.resource.model.QuotaDefinition;
import com.uros.resource.model.Resource;
import com.uros.resource.repository.QuotaDefinitionRepository;
import com.uros.resource.service.QuotaDefinitionService;
import com.uros.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for quota-based reservations (train/priority allocation).
 * Resources divided into predefined quota pools.
 */
@Component
@RequiredArgsConstructor
public class QuotaBasedReservationStrategy implements ReservationStrategy {

    private final ReservationRepository reservationRepository;
    private final QuotaDefinitionRepository quotaDefinitionRepository;
    private final QuotaDefinitionService quotaDefinitionService;
    private final ResourceService resourceService;

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        List<QuotaDefinition> quotas = quotaDefinitionRepository.findByResourceId(request.getResourceId());

        List<AvailabilityResponse.AvailableSlot> slots = quotas.stream()
                .filter(q -> q.getCurrentUsage() < q.getMaxAllocation())
                .map(q -> AvailabilityResponse.AvailableSlot.builder()
                        .slotId(q.getId())
                        .label(q.getQuotaName())
                        .remainingCapacity(q.getMaxAllocation() - q.getCurrentUsage())
                        .build())
                .collect(Collectors.toList());

        return AvailabilityResponse.builder()
                .available(!slots.isEmpty())
                .availableCount(slots.stream().mapToInt(AvailabilityResponse.AvailableSlot::getRemainingCapacity).sum())
                .availableSlots(slots)
                .message(slots.isEmpty() ? "All quotas are full" : "Quota availability found")
                .build();
    }

    @Override
    public Reservation hold(ReservationRequest request, int holdDurationMinutes) {
        if (request.getQuotaDefinitionId() == null) {
            throw new BadRequestException("Quota definition ID is required for quota-based reservations");
        }

        Resource resource = resourceService.getEntity(request.getResourceId());

        // Pessimistic lock on quota
        QuotaDefinition quota = quotaDefinitionRepository.findByIdWithLock(request.getQuotaDefinitionId())
                .orElseThrow(() -> new BadRequestException("Quota not found with id: " + request.getQuotaDefinitionId()));

        if (quota.getCurrentUsage() >= quota.getMaxAllocation()) {
            throw new ConflictException("Quota '" + quota.getQuotaName() + "' is fully allocated");
        }

        // Increment usage
        quota.setCurrentUsage(quota.getCurrentUsage() + 1);
        quotaDefinitionRepository.save(quota);

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .userId(request.getUserId())
                .reservationType(ReservationType.QUOTA_BASED)
                .status(ReservationStatus.HELD)
                .quotaDefinition(quota)
                .holdExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes))
                .build();

        return reservationRepository.save(reservation);
    }

    @Override
    public void releaseResources(Reservation reservation) {
        if (reservation.getQuotaDefinition() != null) {
            QuotaDefinition quota = quotaDefinitionRepository.findById(reservation.getQuotaDefinition().getId()).orElse(null);
            if (quota != null && quota.getCurrentUsage() > 0) {
                quota.setCurrentUsage(quota.getCurrentUsage() - 1);
                quotaDefinitionRepository.save(quota);
            }
        }
    }
}

