package ru.practicum.ewm.ewmservice.service;

import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.dto.CategoryNewDto;

import java.util.List;

public interface CategoryApiService {
    CategoryDto addCategory(CategoryNewDto categoryNewDto);

    CategoryDto getCategoryById(Long cId);

    List<CategoryDto> getCategories(int from, int offset);

    void deleteCategoryById(Long cId);

    CategoryDto updateCategoryById(Long cId, CategoryNewDto categoryNewDto);
}