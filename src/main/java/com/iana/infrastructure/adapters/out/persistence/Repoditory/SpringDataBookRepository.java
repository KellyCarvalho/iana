package com.iana.infrastructure.adapters.out.persistence.Repoditory;

import com.iana.infrastructure.adapters.out.persistence.entity.BookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface SpringDataBookRepository  extends JpaRepository<BookJpaEntity, UUID> {
}
