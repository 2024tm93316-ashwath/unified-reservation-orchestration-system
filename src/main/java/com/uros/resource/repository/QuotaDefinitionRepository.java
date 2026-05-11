package com.uros.resource.repository;

import com.uros.resource.model.QuotaDefinition;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaDefinitionRepository extends JpaRepository<QuotaDefinition, Long> {
    List<QuotaDefinition> findByResourceId(Long resourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QuotaDefinition q WHERE q.id = :id")
    Optional<QuotaDefinition> findByIdWithLock(@Param("id") Long id);
}

