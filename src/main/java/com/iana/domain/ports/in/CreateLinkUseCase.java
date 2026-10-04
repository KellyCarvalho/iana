package com.iana.domain.ports.in;

import com.iana.domain.model.Link;

public interface CreateLinkUseCase {
    Link execute(Link link);
}
