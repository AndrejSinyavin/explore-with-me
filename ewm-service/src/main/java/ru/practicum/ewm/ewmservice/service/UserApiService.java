package ru.practicum.ewm.ewmservice.service;

import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.dto.UserDto;

import java.util.List;

public interface UserApiService {
    UserDto addUser(UserNewDto userNewDto);

    List<UserDto> getUsers(SearchCriteria searchCriteria);

    void deleteUser(Long userId);

    record SearchCriteria(Integer[] ids, Integer pageFrom, Integer pageSize) {
    }
}
