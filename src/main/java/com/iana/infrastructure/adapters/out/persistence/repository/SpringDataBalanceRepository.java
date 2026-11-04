package com.iana.infrastructure.adapters.out.persistence.repository;

import com.iana.infrastructure.adapters.out.persistence.entity.BalanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataBalanceRepository extends JpaRepository<BalanceJpaEntity, UUID> {
    List<BalanceJpaEntity> findByOwnerEmail(String ownerEmail);
    Optional<BalanceJpaEntity> findByIdAndOwnerEmail(UUID id, String ownerEmail);
}
