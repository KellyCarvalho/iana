package com.iana.domain.ports.in;

import com.iana.domain.model.Category;

public interface CreateCategoryUseCase {
    Category execute(Category category);
}
