package ru.practicum.model.server;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "stats-db")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EndpointHitEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long id;

    @Column(nullable = false)
    String app;

    @Column(nullable = false)
    String uri;

    @Column(nullable = false)
    String ip;

    @Column(nullable = false)
    Instant timestamp;

}
