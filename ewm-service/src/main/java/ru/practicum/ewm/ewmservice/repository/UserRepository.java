package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.entity.UserEntity;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

     List<UserDto> getAllByIdInOrderById(Collection<Long> id);

     @Query("select u from UserEntity u order by u.id limit :limit offset :offset")
     List<UserDto> getPageOrderedByIdAsc(Integer limit, Integer offset);

}
