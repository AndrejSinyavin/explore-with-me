package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.ewmservice.dto.CategoryDto;
import ru.practicum.ewm.ewmservice.entity.CategoryEntity;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("select c from CategoryEntity c order by c.id limit :limit offset :offset")
    List<CategoryDto> getPageOrderedByIdAsc(Integer offset, Integer limit);

    @Transactional
    @Modifying
    @Query("update CategoryEntity e set e.name = ?1 where e.id = ?2")
    void updateNameById(String name, Long id);
}