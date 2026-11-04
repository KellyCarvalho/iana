package com.iana.infrastructure.adapters.out.persistence.mapper;

import com.iana.domain.model.Balance;
import com.iana.infrastructure.adapters.out.persistence.entity.BalanceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BalancePersistenceMapper {

    public BalanceJpaEntity toEntity(Balance domain) {
        if (domain == null) return null;

        return new BalanceJpaEntity(
                domain.getId(),
                domain.getSource(),
                domain.getAmount(),
                domain.getDescription(),
                domain.getCreatedAt(),
                domain.getOwnerEmail()
        );
    }

    public Balance toDomain(BalanceJpaEntity entity) {
        if (entity == null) return null;

        return new Balance(
                entity.getId(),
                entity.getSource(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getOwnerEmail()
        );
    }
}
