package com.iana.infrastructure.adapters.out.persistence;

import com.iana.domain.model.Balance;
import com.iana.domain.ports.out.BalanceRepositoryPort;
import com.iana.infrastructure.adapters.out.persistence.entity.BalanceJpaEntity;
import com.iana.infrastructure.adapters.out.persistence.mapper.BalancePersistenceMapper;
import com.iana.infrastructure.adapters.out.persistence.repository.SpringDataBalanceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BalancePersistenceAdapter implements BalanceRepositoryPort {

    private final SpringDataBalanceRepository repository;
    private final BalancePersistenceMapper mapper;

    public BalancePersistenceAdapter(SpringDataBalanceRepository repository, BalancePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Balance save(Balance balance) {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        if (currentUser != null && (balance.getOwnerEmail() == null || balance.getOwnerEmail().isBlank())) {
            balance.setOwnerEmail(currentUser);
        }
        BalanceJpaEntity entity = mapper.toEntity(balance);
        if (currentUser != null && (entity.getOwnerEmail() == null || entity.getOwnerEmail().isBlank())) {
            entity.setOwnerEmail(currentUser);
        }
        BalanceJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Balance> findById(UUID id) {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        if (currentUser != null) {
            return repository.findByIdAndOwnerEmail(id, currentUser).map(mapper::toDomain);
        }
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Balance> findAll() {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        List<BalanceJpaEntity> entities = (currentUser != null)
                ? repository.findByOwnerEmail(currentUser)
                : repository.findAll();
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        String currentUser = SecurityContextUserProvider.getCurrentUserEmail();
        if (currentUser != null) {
            repository.findByIdAndOwnerEmail(id, currentUser)
                    .ifPresent(entity -> repository.deleteById(entity.getId()));
            return;
        }
        repository.deleteById(id);
    }
}
