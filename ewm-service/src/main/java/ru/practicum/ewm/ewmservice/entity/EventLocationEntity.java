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

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventEntity events;

    public LocationDto toDto() {
        return new LocationDto(lat, lon);
    }
}