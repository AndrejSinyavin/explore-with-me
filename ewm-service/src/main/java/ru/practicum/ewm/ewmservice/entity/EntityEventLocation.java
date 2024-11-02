package ru.practicum.ewm.ewmservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.ewmservice.dto.LocationDto;

@Getter
@Setter
@Entity
@Table(name = "locations", schema = "public")
public class EntityEventLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "lat", nullable = false)
    private Float lat;

    @NotNull
    @Column(name = "lon", nullable = false)
    private Float lon;

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private EntityEvent events;

    public LocationDto toDto() {
        return new LocationDto(lat, lon);
    }
}