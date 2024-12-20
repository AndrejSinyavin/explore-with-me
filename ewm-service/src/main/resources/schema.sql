CREATE TABLE IF NOT EXISTS public.categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(50)                             NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uc_categories_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS public.locations
(
    id  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    lat FLOAT                                   NOT NULL,
    lon FLOAT                                   NOT NULL,
    CONSTRAINT pk_locations PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    email VARCHAR(254)                            NOT NULL,
    name  VARCHAR(250)                            NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uc_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS public.events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000)                           NOT NULL,
    category           BIGINT                                  NOT NULL,
    confirmed_requests BIGINT,
    created_on         TIMESTAMP WITHOUT TIME ZONE,
    description        VARCHAR(7000),
    event_date         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    initiator          BIGINT                                  NOT NULL,
    location           BIGINT                                  NOT NULL,
    paid               BOOLEAN                                 NOT NULL,
    participant_limit  INTEGER,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    state              SMALLINT,
    title              VARCHAR(120)                            NOT NULL,
    views              BIGINT,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT uc_events_location UNIQUE (location),
    CONSTRAINT fk_events_on_category FOREIGN KEY (category)
        REFERENCES public.categories (id) ON DELETE SET NULL,
    CONSTRAINT fk_events_on_initiator FOREIGN KEY (initiator)
        REFERENCES public.users (id) ON DELETE CASCADE,
    CONSTRAINT fk_events_on_location FOREIGN KEY (location)
        REFERENCES public.locations (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS public.participation_request
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event     BIGINT,
    status    SMALLINT,
    requester BIGINT,
    created   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_participation_request PRIMARY KEY (id),
    CONSTRAINT uc_participation_request_entity UNIQUE (event, requester),
    CONSTRAINT fk_participation_request_on_event FOREIGN KEY (event)
        REFERENCES public.events (id) ON DELETE CASCADE,
    CONSTRAINT fk_participation_request_on_requester FOREIGN KEY (requester)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned BOOLEAN                                 NOT NULL,
    title  VARCHAR(250)                            NOT NULL,
    CONSTRAINT pk_compilations PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.comp_events
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    compilation  BIGINT NOT NULL,
    event        BIGINT NOT NULL,
    CONSTRAINT pc_comp_events PRIMARY KEY (id),
    CONSTRAINT uc_comp_event UNIQUE (compilation, event),
    CONSTRAINT fc_comps_events_on_compilation FOREIGN KEY (compilation)
        REFERENCES public.compilations (id) ON DELETE CASCADE,
    CONSTRAINT fc_comps_events_on_event FOREIGN KEY (event)
        REFERENCES public.events (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.expectation_event_rating
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_id BIGINT                                  NOT NULL,
    user_id  BIGINT,
    CONSTRAINT pk_expectation_event_rating PRIMARY KEY (id),
    CONSTRAINT uc_expectation_event_to_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_expectation_event_rating_on_event FOREIGN KEY (event_id)
        REFERENCES public.events (id) ON DELETE CASCADE,
    CONSTRAINT fk_expectation_event_rating_on_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS public.event_stat
(
    id                  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_id            BIGINT                                  NOT NULL,
    expectation_rate    BIGINT                                  NOT NULL,
    satisfaction_rate   BIGINT                                  NOT NULL,
    CONSTRAINT pk_expectation_count PRIMARY KEY (id),
    CONSTRAINT uc_event_expectation UNIQUE (event_id),
    CONSTRAINT fc_event_id_to_event FOREIGN KEY (event_id)
        REFERENCES public.events (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.satisfaction_event_rating
(
    id                  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_id            BIGINT                                  NOT NULL,
    user_id             BIGINT,
    satisfaction_rating INTEGER                                 NOT NULL,
    CONSTRAINT pk_satisfaction_event_rating PRIMARY KEY (id),
    CONSTRAINT uc_event_satisfaction UNIQUE (event_id, user_id),
    CONSTRAINT fk_event_satisfaction_event_rating_on_event FOREIGN KEY (event_id)
        REFERENCES public.events (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_satisfaction_event_rating_on_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE SET NULL
);