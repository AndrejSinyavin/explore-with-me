package ru.practicum.ewm.ewmservice.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.ewm.ewmservice.dto.UserNewDto;
import ru.practicum.ewm.ewmservice.service.UserApiService.SearchCriteria;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@Import({EwmServiceImpl.class})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@DisplayName("Интеграционное тестирование реализаций методов интерфейса UserAPI в сервисном слое 'основного сервиса'")
class EwmServiceImplTest {
    private final EwmService ewmService;
    private final SearchCriteria defaultDiapason = new SearchCriteria(null, 0, 10);
    private final UserNewDto userOne = new UserNewDto("user1@mail.ru", "user1");
    private final UserNewDto userTwo = new UserNewDto("user2@mail.ru", "user2");
    private final UserNewDto userThree = new UserNewDto("user3@mail.ru", "user3");

    @Test
    @DisplayName("Комплексный сценарий проверки методов чтения-записи пользователя в репозиторий")
    void addUserAndGetUsersTest() {
        var users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(0));
        var userDb = ewmService.addUser(userOne);
        assertThat(userDb, notNullValue());
        var uid = userDb.id();
        assertThat(userDb.name(), is("user1"));
        assertThat(userDb.email(), is("user1@mail.ru"));
        users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(1));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(userDb.id()));
        assertThat(users.getFirst().name(), is(userDb.name()));
        assertThat(users.getFirst().email(), is(userDb.email()));
        var ids = new SearchCriteria(new Integer[]{Math.toIntExact(uid)}, null, null);
        users = ewmService.getUsers(ids);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(1));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(userDb.id()));
        assertThat(users.getFirst().name(), is(userDb.name()));
        assertThat(users.getFirst().email(), is(userDb.email()));
        userDb = ewmService.addUser(userTwo);
        assertThat(userDb, notNullValue());
        assertThat(userDb.id(), is(uid + 1));
        users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(uid));
        assertThat(users.get(1), notNullValue());
        assertThat(users.get(1).id(), is(uid + 1));
    }

    @Test
    @DisplayName("Сценарий проверки двух режимов работы метода чтения пользователя из репозитория")
    void getUsersTest() {
        var uid = ewmService.addUser(userOne).id();
        ewmService.addUser(userTwo);
        ewmService.addUser(userThree);
        var users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(3));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(uid));
        assertThat(users.getFirst().name(), is(userOne.name()));
        assertThat(users.getFirst().email(), is(userOne.email()));
        assertThat(users.get(1), notNullValue());
        assertThat(users.get(1).id(), is(uid + 1));
        assertThat(users.get(2), notNullValue());
        assertThat(users.get(2).id(), is(uid + 2));
        users = ewmService.getUsers(new SearchCriteria(new Integer[]{}, 1, 10));
        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(uid + 1));
        assertThat(users.get(1), notNullValue());
        assertThat(users.get(1).id(), is(uid + 2));
    }

    @Test
    @DisplayName("Сценарий проверки метода удаления пользователя из репозитория")
    void deleteUserTest() {
        var users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(0));
        var userDb = ewmService.addUser(userOne);
        assertThat(userDb, notNullValue());
        var uid = userDb.id();
        assertThat(userDb.name(), is("user1"));
        assertThat(userDb.email(), is("user1@mail.ru"));
        ewmService.addUser(userTwo);
        users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        users = ewmService.getUsers(
                new SearchCriteria(
                        new Integer[]{Math.toIntExact(uid), Math.toIntExact(uid + 1)},
                        null,
                        null
                )
        );
        assertThat(users, notNullValue());
        assertThat(users.size(), is(2));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(uid));
        assertThat(users.getLast(), notNullValue());
        assertThat(users.getLast().id(), is(uid + 1));
        ewmService.deleteUser(uid);
        users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(1));
        assertThat(users.getFirst(), notNullValue());
        assertThat(users.getFirst().id(), is(uid + 1));
        ewmService.deleteUser(uid + 1);
        users = ewmService.getUsers(defaultDiapason);
        assertThat(users, notNullValue());
        assertThat(users.size(), is(0));
    }
}