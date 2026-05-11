package com.uros.resource.model;

import com.uros.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quota_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "quota_name", nullable = false)
    private String quotaName;

    @Column(name = "max_allocation", nullable = false)
    private Integer maxAllocation;

    @Column(name = "current_usage", nullable = false)
    @Builder.Default
    private Integer currentUsage = 0;
}

