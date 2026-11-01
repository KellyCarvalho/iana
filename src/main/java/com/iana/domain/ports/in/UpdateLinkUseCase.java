package com.iana.domain.ports.in;

import com.iana.domain.model.Link;

import java.util.UUID;

public interface UpdateLinkUseCase {
    Link update(UUID id, Link link);
}
