CREATE TABLE Users (
                       user_id SERIAL PRIMARY KEY,
                       email TEXT UNIQUE NOT NULL,
                       password TEXT NOT NULL,
                       name TEXT NOT NULL
);

CREATE TABLE Location (
                          location_id SERIAL PRIMARY KEY,
                          latitude DOUBLE PRECISION NOT NULL,
                          longitude DOUBLE PRECISION NOT NULL,
                          address TEXT,
                          name TEXT
);

CREATE TABLE Clients (
                         user_id INT PRIMARY KEY REFERENCES Users(user_id),
                         address TEXT NOT NULL
);

CREATE TABLE Couriers (
                          user_id INT PRIMARY KEY REFERENCES Users(user_id),
                          current_location INT REFERENCES Location(location_id),
                          is_available BOOLEAN DEFAULT TRUE
);

CREATE TABLE Request (
                         request_id SERIAL PRIMARY KEY,
                         client_id INT REFERENCES Clients(user_id),
                         courier_id INT REFERENCES Couriers(user_id),
                         created_at TIMESTAMP DEFAULT NOW(),
                         request_status TEXT CHECK (request_status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'HEADING_TO_PICKUP', 'PICKED_UP', 'COMPLETED')),
                         ETA INTERVAL
);

CREATE TABLE RequestDetails (
                                request_id INT PRIMARY KEY REFERENCES Request(request_id),
                                description TEXT,
                                pickup_location_id INT REFERENCES Location(location_id),
                                dropoff_location_id INT REFERENCES Location(location_id)
);

CREATE TABLE Delivery (
                          delivery_id SERIAL PRIMARY KEY,
                          request_id INT UNIQUE REFERENCES Request(request_id),
                          started_at TIMESTAMP,
                          completed_at TIMESTAMP,
                          delivery_status TEXT CHECK (delivery_status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE PickupSpot (
                            location_id INT PRIMARY KEY REFERENCES Location(location_id),
                            latitude DOUBLE PRECISION NOT NULL,
                            longitude DOUBLE PRECISION NOT NULL,
                            address TEXT
);