package ru.practicum.ewm.ewmservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.ewmservice.dto.LocationDto;

@Getter
@Setter
@Entity
@Table(name = "locations", schema = "public")
public class EventLocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "lat")
    private Float lat;

    @NotNull
    @Column(name = "lon")
    private Float lon;

    public LocationDto toDto() {
        return new LocationDto(lat, lon);
    }
}