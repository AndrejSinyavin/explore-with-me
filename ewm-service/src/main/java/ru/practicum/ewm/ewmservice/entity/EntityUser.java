package ru.practicum.ewm.ewmservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.ewmservice.dto.UserDto;
import ru.practicum.ewm.ewmservice.dto.UserShortDto;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uc_users_email", columnNames = {"email"})
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntityUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "email", nullable = false, length = 254)
    String email;

    @Column(name = "name", nullable = false, length = 250)
    String name;

    @OneToMany(mappedBy = "initiator",
            cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<EntityEvent> entityEvents = new LinkedHashSet<>();

    public UserDto toUserDto() {
        return new UserDto(id, email, name);
    }

    public UserShortDto toUserShortDto() {
        return new UserShortDto(id, name);
    }
}