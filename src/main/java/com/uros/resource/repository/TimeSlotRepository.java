package com.uros.resource.repository;

import com.uros.resource.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByResourceIdAndIsActiveTrue(Long resourceId);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.resource.id = :resourceId " +
           "AND ts.isActive = true AND ts.startTime >= :startTime AND ts.endTime <= :endTime")
    List<TimeSlot> findAvailableSlots(@Param("resourceId") Long resourceId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);
}

