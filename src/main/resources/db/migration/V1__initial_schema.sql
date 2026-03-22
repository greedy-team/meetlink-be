CREATE TABLE meeting (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(255) NOT NULL UNIQUE,
    enable_time_recommendation  BOOLEAN NOT NULL DEFAULT FALSE,
    enable_place_recommendation BOOLEAN NOT NULL DEFAULT FALSE,
    time_availability_type      VARCHAR(255),
    time_range_start            TIME,
    time_range_end              TIME,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);

CREATE TABLE participant (
    id                    BIGSERIAL PRIMARY KEY,
    meeting_id            BIGINT NOT NULL REFERENCES meeting(id),
    nickname              VARCHAR(255) NOT NULL,
    token                 VARCHAR(255) NOT NULL,
    time_submitted_at     TIMESTAMP,
    location_submitted_at TIMESTAMP,
    created_at            TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP,
    CONSTRAINT uk_participant_meeting_token    UNIQUE (meeting_id, token),
    CONSTRAINT uk_participant_meeting_nickname UNIQUE (meeting_id, nickname)
);

CREATE INDEX idx_participant_meeting ON participant(meeting_id);
CREATE INDEX idx_participant_meeting_time_submitted ON participant(meeting_id, time_submitted_at);
CREATE INDEX idx_participant_meeting_location_submitted ON participant(meeting_id, location_submitted_at);
CREATE INDEX idx_participant_meeting_order ON participant(meeting_id, created_at, nickname);

CREATE TABLE time_availability (
    id             BIGSERIAL PRIMARY KEY,
    meeting_id     BIGINT NOT NULL REFERENCES meeting(id),
    participant_id BIGINT NOT NULL REFERENCES participant(id) ON DELETE CASCADE,
    date           DATE,
    day_of_week    INTEGER,
    start_time     TIME NOT NULL,
    CONSTRAINT uk_time_availability UNIQUE (meeting_id, participant_id, date, day_of_week, start_time)
);

CREATE INDEX idx_time_meeting_participant ON time_availability(meeting_id, participant_id);
CREATE INDEX idx_time_meeting ON time_availability(meeting_id);
CREATE INDEX idx_time_meeting_start_time ON time_availability(meeting_id, start_time);

CREATE TABLE location_availability (
    id             BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL UNIQUE REFERENCES participant(id) ON DELETE CASCADE,
    name           VARCHAR(255),
    address        VARCHAR(255),
    latitude       DOUBLE PRECISION NOT NULL DEFAULT 0,
    longitude      DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE TABLE time_candidate (
    id              BIGSERIAL PRIMARY KEY,
    meeting_id      BIGINT NOT NULL REFERENCES meeting(id),
    date            DATE,
    day_of_week     INTEGER,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    available_count INTEGER NOT NULL,
    rank            INTEGER NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_time_candidate_meeting_rank ON time_candidate(meeting_id, rank);
CREATE INDEX idx_time_candidate_meeting_created_at ON time_candidate(meeting_id, created_at);

CREATE TABLE place_candidate (
    id              BIGSERIAL PRIMARY KEY,
    meeting_id      BIGINT NOT NULL REFERENCES meeting(id),
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(255) NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    avg_travel_time DOUBLE PRECISION NOT NULL,
    max_travel_time DOUBLE PRECISION NOT NULL,
    rank            INTEGER NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_place_candidate_meeting_rank ON place_candidate(meeting_id, rank);
CREATE INDEX idx_place_candidate_meeting_created_at ON place_candidate(meeting_id, created_at);

CREATE TABLE place_candidate_route (
    id                 BIGSERIAL PRIMARY KEY,
    place_candidate_id BIGINT NOT NULL REFERENCES place_candidate(id) ON DELETE CASCADE,
    participant_id     BIGINT NOT NULL REFERENCES participant(id) ON DELETE CASCADE,
    travel_time        DOUBLE PRECISION NOT NULL,
    segments           TEXT NOT NULL,
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP
);

CREATE INDEX idx_place_candidate_route_candidate ON place_candidate_route(place_candidate_id);

CREATE TABLE meeting_result (
    id                 BIGSERIAL PRIMARY KEY,
    meeting_id         BIGINT NOT NULL UNIQUE REFERENCES meeting(id),
    time_candidate_id  BIGINT REFERENCES time_candidate(id) ON DELETE SET NULL,
    place_candidate_id BIGINT REFERENCES place_candidate(id) ON DELETE SET NULL,
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP
);
