package com.uros.resource.repository;

import com.uros.resource.model.ResourceType;
import com.uros.common.enums.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {
    Optional<ResourceType> findByName(String name);
    List<ResourceType> findByReservationType(ReservationType reservationType);
}

