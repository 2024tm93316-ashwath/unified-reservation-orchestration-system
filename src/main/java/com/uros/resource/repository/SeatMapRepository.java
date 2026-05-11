package com.uros.resource.repository;

import com.uros.resource.model.SeatMap;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatMapRepository extends JpaRepository<SeatMap, Long> {
    List<SeatMap> findByResourceId(Long resourceId);

    List<SeatMap> findByResourceIdAndIsAvailableTrue(Long resourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatMap s WHERE s.id = :id")
    Optional<SeatMap> findByIdWithLock(@Param("id") Long id);
}

