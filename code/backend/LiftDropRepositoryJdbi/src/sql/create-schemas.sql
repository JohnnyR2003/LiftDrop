drop schema if exists liftdrop cascade;
create schema liftdrop;
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
    address                         INT,
    name                            TEXT,
    FOREIGN KEY (address) REFERENCES liftdrop.address(address_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.client(
    client_id                         INT PRIMARY KEY,
    address                         INT,
    FOREIGN KEY (client_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (address) REFERENCES liftdrop.address(address_id) ON DELETE SET NULL
);


CREATE TABLE liftdrop.courier (
    courier_id                         INT PRIMARY KEY,
    current_location                INT,
    is_available                    BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (courier_id) REFERENCES liftdrop.user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (current_location)  REFERENCES liftdrop.location (location_id) ON DELETE SET NULL
);

CREATE TABLE liftdrop.request (
    request_id                      SERIAL PRIMARY KEY,
    client_id                       INT,
    courier_id                      INT,
    created_at                      TIMESTAMP DEFAULT NOW(),
    request_status                  TEXT CHECK (request_status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'HEADING_TO_PICKUP', 'PICKED_UP', 'COMPLETED')),
    ETA                             INTERVAL,
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

CREATE TABLE liftdrop.delivery (
    delivery_id                     SERIAL PRIMARY KEY,
    request_id                      INT,
    started_at                      TIMESTAMP,
    completed_at                    TIMESTAMP,
    delivery_status                 TEXT CHECK (delivery_status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    FOREIGN KEY (request_id) REFERENCES liftdrop.request(request_id) ON DELETE CASCADE
);

CREATE TABLE liftdrop.pickup_spot () INHERITS (liftdrop.location);

ALTER TABLE liftdrop.pickup_spot
    ADD CONSTRAINT pickup_spot_location_id_pk PRIMARY KEY (location_id);

CREATE TABLE liftdrop.dropoff_spot (
    client_id                       INT,
    FOREIGN KEY (client_id) REFERENCES liftdrop.client(client_id) ON DELETE CASCADE
    )INHERITS (liftdrop.location);

CREATE TABLE liftdrop.item (
    item_id                         SERIAL PRIMARY KEY,
    establishment                   TEXT,
    establishment_location          INT,
    designation                     TEXT,
    price                           NUMERIC,
    ETA                             INTERVAL,
    FOREIGN KEY (establishment_location) REFERENCES liftdrop.pickup_spot(location_id) ON DELETE CASCADE
)