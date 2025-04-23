-- Insert users first (no dependencies)
INSERT INTO liftdrop."user" (user_id, email, password, name, role)
VALUES (1, 'john.doe@example.com', 'hashed_password_1', 'John Doe', 'CLIENT'),
       (2, 'jane.smith@example.com', 'hashed_password_2', 'Jane Smith', 'CLIENT'),
       (3, 'michael.jones@example.com', 'hashed_password_3', 'Michael Jones', 'CLIENT'),
       (4, 'john.doe2@example.com', 'hashed_password_4', 'John Doe2', 'COURIER'),
       (5, 'jane.smith2@example.com', 'hashed_password_5', 'Jane Smith2', 'COURIER'),
       (6, 'michael.jones2@example.com', 'hashed_password_6', 'Michael Jones2', 'COURIER'),
       (7, 'courier7@example.com', 'hashed_password_7', 'Courier Seven', 'COURIER'),
       (8, 'courier8@example.com', 'hashed_password_8', 'Courier Eight', 'COURIER'),
       (9, 'courier9@example.com', 'hashed_password_9', 'Courier Nine', 'COURIER'),
       (10, 'courier10@example.com', 'hashed_password_10', 'Courier Ten', 'COURIER');



--Insert Addresses
INSERT INTO liftdrop."address" (address_id, country, city, street, house_number, floor, zip_code)
VALUES (1, 'USA', 'New York', '5th Avenue', '123', '2nd', '10001'),
       (2, 'USA', 'Los Angeles', 'Sunset Boulevard', '456', '3rd', '90001'),
       (3, 'USA', 'San Francisco', 'Market Street', '789', '4th', '94101');

-- Insert locations next (no dependencies)
INSERT INTO liftdrop."location" (location_id, latitude, longitude, address, name)
VALUES (1, 40.7128, -74.0060, 1, 'New York City'),
       (2, 34.0522, -118.2437, 2, 'Los Angeles'),
       (3, 37.7749, -122.4194, 3, 'San Francisco'),
       (4, 40.7306, -73.9352, 1, 'New York City'),
       (5, 34.0522, -118.2437, 2, 'Los Angeles'),
       (6, 37.7749, -122.4194, 3, 'San Francisco'),
       (7, 38.73908, -9.12461, 1, 'Olaias'),
       (8, 38.74362, -9.13896, 1, 'Avenida de Roma'),
       (9, 38.74140, -9.14667, 1, 'Campo Pequeno');



-- Insert clients (depends on Users)
INSERT INTO liftdrop."client" (client_id, address)
VALUES (1, 1),
       (2, 2),
       (3, 3);

-- Insert couriers (depends on Users and Location)
INSERT INTO liftdrop."courier" (courier_id, current_location, is_available)
VALUES (4, 1, true),
       (5, 2, false),
       (6, 3, false),
       (7, 4, false),
       (8, 7, true),
       (9, 8, true),
       (10, 9, true);


-- Insert requests (depends on Clients and Couriers)
INSERT INTO liftdrop."request" (request_id, client_id, courier_id, created_at, request_status, ETA)
VALUES (1, 1, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 1800),
       (2, 2, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 2700),
       (3, 3, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 3600),
       (4, 1, 4, EXTRACT(EPOCH FROM NOW()), 'PENDING', 1800);

-- Insert request details (depends on Request and Location)
INSERT INTO liftdrop."request_details" (request_id, description, pickup_location, dropoff_location)
VALUES (1, 'Pickup documents', 1, 2),
       (2, 'Deliver food package', 2, 3);

INSERT INTO liftdrop."pickup_spot" (location_id, latitude, longitude, address, name)
VALUES (1, 40.7128, -74.0060, 1, 'Pickup Spot 1'),
       (2, 34.0522, -118.2437, 2, 'Pickup Spot 2'),
       (3, 37.7749, -122.4194, 3, 'Pickup Spot 3');

INSERT INTO liftdrop."item" (item_id, establishment, establishment_location, designation, price, ETA)
VALUES (1, 'MC DONALDS CHELAS', 1, 'Big Mac', 5.99, 1800),
       (2, 'BURGER KING SALDANHA', 2, 'Whopper', 6.49, 2700),
       (3, 'KFC COLOMBO', 3, 'Zinger', 7.99, 3600);
--deliveries todo

INSERT INTO liftdrop."delivery" (delivery_id, courier_id, request_id, started_at, completed_at, ETA, delivery_status)
VALUES (1, 4, 3, EXTRACT(EPOCH FROM NOW()), NULL, 1800, 'IN_PROGRESS');
--      (2, 4, 1, NOW(), NULL, INTERVAL '30 minutes', 'IN_PROGRESS'),
--      (2, 5, 2, NOW(), NULL, INTERVAL '45 minutes', 'IN_PROGRESS'),
--      (3, 6, 3, NOW(), NULL, INTERVAL '1 hour', 'IN_PROGRESS');
SELECT setval(pg_get_serial_sequence('liftdrop.user', 'user_id'), (SELECT MAX(user_id) FROM liftdrop.user));

SELECT setval(pg_get_serial_sequence('liftdrop.address', 'address_id'), (SELECT MAX(address_id) FROM liftdrop.address));

SELECT setval(pg_get_serial_sequence('liftdrop.location', 'location_id'), (SELECT MAX(location_id) FROM liftdrop.location));

SELECT setval(pg_get_serial_sequence('liftdrop.request', 'request_id'), (SELECT MAX(request_id) FROM liftdrop.request));

SELECT setval(pg_get_serial_sequence('liftdrop.delivery', 'delivery_id'), (SELECT MAX(delivery_id) FROM liftdrop.delivery));
