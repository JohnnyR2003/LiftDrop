-- Insert users first (no dependencies)
INSERT INTO liftdrop."user" (user_id, email, password, name, role)
VALUES (1, 'john.doe@example.com', 'hashed_password_1', 'John Doe', 'CLIENT'),
       (2, 'jane.smith@example.com', 'hashed_password_2', 'Jane Smith', 'CLIENT'),
       (3, 'michael.jones@example.com', 'hashed_password_3', 'Michael Jones', 'CLIENT'),
       (4, 'john.doe2@example.com', 'hashed_password_4', 'John Doe2', 'COURIER'),
       (5, 'jane.smith2@example.com', 'hashed_password_5', 'Jane Smith2', 'COURIER'),
       (6, 'michael.jones2@example.com', 'hashed_password_6', 'Michael Jones2', 'COURIER');



--Insert Addresses
INSERT INTO liftdrop."address" (address_id, country, city, street, house_number, floor, zip_code)
VALUES (1, 'USA', 'New York', '5th Avenue', '123', '2nd', '10001'),
       (2, 'USA', 'Los Angeles', 'Sunset Boulevard', '456', '3rd', '90001'),
       (3, 'USA', 'San Francisco', 'Market Street', '789', '4th', '94101');

-- Insert locations next (no dependencies)
INSERT INTO liftdrop."location" (location_id, latitude, longitude, address, name)
VALUES (1, 40.7128, -74.0060, 1, 'New York City'),
       (2, 34.0522, -118.2437, 2, 'Los Angeles'),
       (3, 37.7749, -122.4194, 3, 'San Francisco');

-- Insert clients (depends on Users)
INSERT INTO liftdrop."client" (client_id, address)
VALUES (1, 1),
       (2, 2),
       (3, 3);

-- Insert couriers (depends on Users and Location)
INSERT INTO liftdrop."courier" (courier_id, current_location, is_available)
VALUES (4, 1, false),
       (5, 2, false),
       (6, 3, false);

-- Insert requests (depends on Clients and Couriers)
INSERT INTO liftdrop."request" (request_id, client_id, courier_id, created_at, request_status, ETA)
VALUES (1, 1, NULL, NOW(), 'PENDING', INTERVAL '30 minutes'),
       (2, 2, NULL, NOW(), 'PENDING', INTERVAL '45 minutes'),
       (3, 3, NULL, NOW(), 'PENDING', INTERVAL '1 hour');

-- Insert request details (depends on Request and Location)
INSERT INTO liftdrop."request_details" (request_id, description, pickup_location, dropoff_location)
VALUES (1, 'Pickup documents', 1, 2),
       (2, 'Deliver food package', 2, 3);

INSERT INTO liftdrop."pickup_spot" (location_id, latitude, longitude, address, name)
VALUES (1, 40.7128, -74.0060, 1, 'Pickup Spot 1'),
       (2, 34.0522, -118.2437, 2, 'Pickup Spot 2'),
       (3, 37.7749, -122.4194, 3, 'Pickup Spot 3');

INSERT INTO liftdrop."item" (item_id, establishment, establishment_location, designation, price, ETA)
VALUES (1, 'MC DONALDS CHELAS', 1, 'Big Mac', 5.99, INTERVAL '30 minutes'),
       (2, 'BURGER KING SALDANHA', 2, 'Whopper', 6.49, INTERVAL '45 minutes'),
       (3, 'KFC COLOMBO', 3, 'Zinger', 7.99, INTERVAL '1 hour');
--deliveries todo

SELECT setval(pg_get_serial_sequence('liftdrop.user', 'user_id'), (SELECT MAX(user_id) FROM liftdrop.user));

SELECT setval(pg_get_serial_sequence('liftdrop.address', 'address_id'), (SELECT MAX(address_id) FROM liftdrop.address));

SELECT setval(pg_get_serial_sequence('liftdrop.location', 'location_id'), (SELECT MAX(location_id) FROM liftdrop.location));