package com.swiftbite.restaurant.service;

import com.swiftbite.restaurant.dto.CategoryResponse;
import com.swiftbite.restaurant.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> new CategoryResponse(cat.getId(), cat.getName(), cat.getIcon()))
                .collect(Collectors.toList());
    }
}
