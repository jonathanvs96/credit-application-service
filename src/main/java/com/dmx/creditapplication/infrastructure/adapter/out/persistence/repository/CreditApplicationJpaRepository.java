package com.dmx.creditapplication.infrastructure.adapter.out.persistence.repository;

import com.dmx.creditapplication.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditApplicationJpaRepository extends JpaRepository<CreditApplicationEntity, UUID> {
}
