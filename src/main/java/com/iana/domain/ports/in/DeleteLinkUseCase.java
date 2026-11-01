package com.iana.domain.ports.in;

import java.util.UUID;

public interface DeleteLinkUseCase {
    void deleteById(UUID id);
}
