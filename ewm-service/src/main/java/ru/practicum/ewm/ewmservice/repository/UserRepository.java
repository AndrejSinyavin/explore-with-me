package ru.practicum.ewm.ewmservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.entity.EntityUser;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<EntityUser, Long> {

     List<UserDto> getAllByIdInOrderById(Collection<Long> id);

     @Query("select u from EntityUser u order by u.id limit :limit offset :offset")
     List<UserDto> getPageOrderedByIdAsc(Integer limit, Integer offset);

}
