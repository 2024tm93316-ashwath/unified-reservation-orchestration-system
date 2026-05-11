package com.uros.resource.repository;

import com.uros.resource.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByResourceTypeId(Long resourceTypeId);
    List<Resource> findByIsActiveTrue();
    List<Resource> findByResourceType_ReservationType(com.uros.common.enums.ReservationType type);
}

