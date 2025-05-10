drop schema if exists liftdrop cascade;
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;
create schema liftdrop;
SET search_path TO liftdrop;

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

CREATE TABLE liftdrop.client(
                                client_id                       INT PRIMARY KEY,
                                address                         INT,
                                FOREIGN KEY (client_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE,
                                FOREIGN KEY (address) REFERENCES liftdrop.address(address_id) ON DELETE SET NULL
);


CREATE TABLE liftdrop.courier (
                                  courier_id                         INT PRIMARY KEY,
                                  current_location                INT DEFAULT NULL,
                                  is_available                    BOOLEAN DEFAULT FALSE,
                                  FOREIGN KEY (courier_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE,
                                  FOREIGN KEY (current_location)  REFERENCES liftdrop.location (location_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.request (
                                  request_id                      SERIAL PRIMARY KEY,
                                  client_id                       INT,
                                  courier_id                      INT,
                                  created_at                      BIGINT DEFAULT EXTRACT(EPOCH FROM NOW()),
                                  request_status                  TEXT CHECK (request_status IN ('PENDING', 'PENDING_CANCELLATION', 'ACCEPTED', 'DECLINED', 'HEADING_TO_PICKUP', 'PICKED_UP', 'COMPLETED')),
                                  ETA                             BIGINT,
                                  FOREIGN KEY (client_id) REFERENCES liftdrop.client(client_id) ON DELETE CASCADE,
                                  FOREIGN KEY (courier_id) REFERENCES liftdrop.courier(courier_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.request_details (
                                          request_id                      INT PRIMARY KEY,
                                          description                     TEXT,
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
                                   delivery_status                 TEXT CHECK (delivery_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED','PICKED_UP','PICKING_UP')),
                                   FOREIGN KEY (courier_id) REFERENCES liftdrop.courier(courier_id) ON DELETE CASCADE,
                                   FOREIGN KEY (request_id) REFERENCES liftdrop.request(request_id) ON DELETE CASCADE
);

-- CREATE TABLE liftdrop.pickup_spot () INHERITS (liftdrop.location);

-- ALTER TABLE liftdrop.pickup_spot
--     ADD CONSTRAINT pickup_spot_location_id_pk PRIMARY KEY (location_id);

CREATE TABLE liftdrop.dropoff_spot (
                                       client_id                       INT,
                                       FOREIGN KEY (client_id) REFERENCES liftdrop.client(client_id) ON DELETE CASCADE
);

CREATE TABLE liftdrop.item (
                               item_id                         SERIAL PRIMARY KEY,
                               establishment                   TEXT,
                               establishment_location          INT,
                               designation                     TEXT,
                               price                           NUMERIC,
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