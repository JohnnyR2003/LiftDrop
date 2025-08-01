DROP SCHEMA IF EXISTS liftdrop CASCADE;
CREATE SCHEMA liftdrop;
SET search_path TO liftdrop;
-- Now create the extensions
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE TABLE liftdrop.user (
                               user_id                         SERIAL UNIQUE PRIMARY KEY,
                               email                           TEXT UNIQUE NOT NULL,
                               password                        TEXT NOT NULL,
                               name                            TEXT NOT NULL,
                               role                            TEXT CHECK (role IN ('ADMIN', 'CLIENT', 'COURIER')) NOT NULL
);

CREATE TABLE liftdrop.address (
                                  address_id SERIAL PRIMARY KEY,
                                  country TEXT NOT NULL,
                                  city TEXT NOT NULL,
                                  street TEXT NOT NULL,
                                  house_number TEXT,
                                  floor TEXT,
                                  zip_code TEXT NOT NULL
);

CREATE TABLE liftdrop.location (
                                   location_id                     SERIAL PRIMARY KEY,
                                   latitude                        DOUBLE PRECISION NOT NULL,
                                   longitude                       DOUBLE PRECISION NOT NULL,
                                   address                         INT DEFAULT NULL,
                                   name                            TEXT,
                                   FOREIGN KEY (address) REFERENCES liftdrop.address(address_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.client (
                                client_id                       INT PRIMARY KEY,
                                address                         INT,
                                FOREIGN KEY (client_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (address) REFERENCES liftdrop.address(address_id) ON DELETE SET NULL
);


CREATE TABLE liftdrop.courier (
                                courier_id                      INT PRIMARY KEY,
                                current_location                INT DEFAULT NULL,
                                is_available                    BOOLEAN DEFAULT FALSE,
                                daily_earnings                  DOUBLE PRECISION,
                                rating                          NUMERIC(2,1) CHECK (rating >= 1.0 AND rating <= 5.0) DEFAULT NULL,
                                FOREIGN KEY (courier_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (current_location)  REFERENCES liftdrop.location (location_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.request (
                                  request_id      SERIAL PRIMARY KEY,
                                  client_id       INT,
                                  courier_id      INT,
                                  created_at      BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                  request_status  TEXT CHECK (request_status IN ('PENDING','HEADING_TO_PICKUP', 'HEADING_TO_DROPOFF', 'DELIVERED', 'PENDING_REASSIGNMENT')),
                                  ETA             BIGINT DEFAULT NULL,
                                  pickup_code     TEXT,
                                  dropoff_code    TEXT,
                                  FOREIGN KEY (client_id) REFERENCES liftdrop.client(client_id) ON DELETE CASCADE,
                                  FOREIGN KEY (courier_id) REFERENCES liftdrop.courier(courier_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.request_details (
                                          request_id                      INT PRIMARY KEY,
                                          description                     TEXT,
                                          quantity                        INT,
                                          pickup_location                 INT ,
                                          dropoff_location                INT ,
                                          FOREIGN KEY (request_id) REFERENCES liftdrop.request(request_id) ON DELETE CASCADE,
                                          FOREIGN KEY (pickup_location) REFERENCES liftdrop.location(location_id) ON DELETE SET NULL,
                                          FOREIGN KEY (dropoff_location) REFERENCES liftdrop.location(location_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.request_declines (
                                           decline_id SERIAL PRIMARY KEY,
                                           request_id INT NOT NULL,
                                           courier_id INT NOT NULL,
                                           declined_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                           FOREIGN KEY (request_id) REFERENCES liftdrop.request(request_id) ON DELETE CASCADE,
                                           FOREIGN KEY (courier_id) REFERENCES liftdrop.courier(courier_id) ON DELETE CASCADE
);

CREATE TABLE liftdrop.delivery (
                                   delivery_id                     SERIAL PRIMARY KEY,
                                   courier_id                      INT,
                                   request_id                      INT,
                                   started_at                      BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                   completed_at                    BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                   ETA                             BIGINT,
                                   delivery_status                 TEXT CHECK (delivery_status IN ('PENDING', 'IN_PROGRESS', 'CANCELLED', 'PICKED_UP', 'DROPPED_OFF')),
                                   FOREIGN KEY (courier_id) REFERENCES liftdrop.courier(courier_id) ON DELETE CASCADE,
                                   FOREIGN KEY (request_id) REFERENCES liftdrop.request(request_id) ON DELETE CASCADE
);

-- CREATE TABLE liftdrop.pickup_spot () INHERITS (liftdrop.location);

-- ALTER TABLE liftdrop.pickup_spot
--     ADD CONSTRAINT pickup_spot_location_id_pk PRIMARY KEY (location_id);

CREATE TABLE liftdrop.dropoff_spot (
                               location_id                 SERIAL PRIMARY KEY,
                               client_id                       INT,
                               FOREIGN KEY (client_id) REFERENCES liftdrop.client(client_id) ON DELETE CASCADE,
                               FOREIGN KEY (location_id) REFERENCES liftdrop.location(location_id) ON DELETE CASCADE
);

CREATE TABLE liftdrop.item (
                               item_id                         SERIAL PRIMARY KEY,
                               establishment                   TEXT,
                               establishment_location          INT,
                               designation                     TEXT,
                               price                           DOUBLE PRECISION,
                               ETA                             BIGINT,
                               FOREIGN KEY (establishment_location) REFERENCES liftdrop.location(location_id) ON DELETE CASCADE
);

CREATE TABLE liftdrop.sessions (
                                   session_id                      SERIAL PRIMARY KEY,
                                   user_id                         INT,
                                   session_token                   TEXT NOT NULL,
                                   created_at                      BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                   role                            TEXT CHECK (role IN ('ADMIN', 'CLIENT', 'COURIER')) NOT NULL,
--     expires_at TIMESTAMP DEFAULT NOW() + INTERVAL '1 hour',
                                   FOREIGN KEY (user_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE
);

CREATE TABLE liftdrop.courier_rating (
                                         rating_id    SERIAL PRIMARY KEY,
                                         courier_id   INT NOT NULL,
                                         request_id   INT,
                                         client_id    INT,
                                         rating       NUMERIC(2,1) CHECK (rating >= 1.0 AND rating <= 5.0) NOT NULL,
                                         comment      TEXT,
                                         created_at   BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                         FOREIGN KEY (courier_id) REFERENCES liftdrop.courier(courier_id) ON DELETE CASCADE,
                                         FOREIGN KEY (request_id) REFERENCES liftdrop.request(request_id) ON DELETE SET NULL,
                                         FOREIGN KEY (client_id) REFERENCES liftdrop.client(client_id) ON DELETE SET NULL
);
ALTER TABLE liftdrop.courier_rating
    ADD CONSTRAINT unique_client_courier_request_rating UNIQUE (client_id, courier_id, request_id);

CREATE INDEX ON liftdrop.item USING GIN (establishment gin_trgm_ops);