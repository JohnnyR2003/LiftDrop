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
VALUES (1, 'PORTUGAL', 'Lisbon', 'Avenida de Roma', '15', 'RC', '1000-264'),
       (2, 'PORTUGAL', 'Lisbon', 'Avenida Fontes Pereira de Melo', '16', 'RC', '1050-116'),
       (3, 'PORTUGAL', 'Lisbon', 'Avenida da República', '12', '1', '1050-191'),
       (4, 'PORTUGAL', 'Lisbon', 'Rua Albert Einstein', '1', '2', '1500-676'),
       (5, 'PORTUGAL', 'Lisbon', 'Avenida Dom João II', '40', '3', '1990-094'),
       (6, 'PORTUGAL', 'Lisbon', 'Avenida da Liberdade', '2', 'RC', '1250-113');


-- Insert locations next (no dependencies)
INSERT INTO liftdrop."location" (location_id, latitude, longitude, address, name)
VALUES (1, 38.743424, -9.138986, 1, 'Avenida de Roma'),
       (2, 38.7267130611275, -9.148917466507026, 2, 'Avenida Fontes Pereira de Melo'),
       (3, 38.73548667805665, -9.144861015848331, 3, 'Avenida da Republica'),
       (4, 38.75504773230685, -9.188669486292124 , 4, 'Centro Colombo'),
       (5, 38.768230407427936, -9.09637567868234, 5, 'Centro Comercial Vasco da Gama'),
       (6, 38.716107488039675, -9.142992520654982, 6, 'Avenida da Liberdade');

-- Insert clients (depends on Users)
INSERT INTO liftdrop."client" (client_id, address)
VALUES (1, 1),
       (2, 2),
       (3, 3);

-- Insert couriers (depends on Users and Location)
INSERT INTO liftdrop."courier" (courier_id, current_location, daily_earnings, is_available)
VALUES (4, 1, 0.00, true),
       (5, 2, 0.00, false),
       (6, 3, 0.00, false),
       (7, 4, 0.00, false),
       (8, 5, 0.00, true),
       (9, 6, 0.00, false),
       (10, 4, 0.00, false);


-- Insert requests (depends on Clients and Couriers)
INSERT INTO liftdrop."request" (request_id, client_id, courier_id, created_at, request_status, ETA)
VALUES (1, 1, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 1800),
       (2, 2, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 2700),
       (3, 3, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 3600),
       (4, 1, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 1800),
       (5, 2, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 2700);


-- Insert request details (depends on Request and Location)
INSERT INTO liftdrop."request_details" (request_id, description, pickup_location, dropoff_location)
VALUES (1, 'Chicken Wings', 1, 2),
       (2, 'Big Mac', 2, 3);

-- INSERT INTO liftdrop."pickup_spot" (location_id, latitude, longitude, address, name)
-- VALUES (1, 40.7128, -74.0060, 1, 'Pickup Spot 1'),
--        (2, 34.0522, -118.2437, 2, 'Pickup Spot 2'),
--        (3, 37.7749, -122.4194, 3, 'Pickup Spot 3'),
--        (4, 38.73908, -9.12461, 4, 'Pickup Spot 4'),
--        (5, 38.74362, -9.13896, 5, 'Pickup Spot 5'),
--        (6, 38.74140, -9.14667, 6, 'Pickup Spot 6'),
--        (7, 38.74000, -9.15000, 7, 'Pickup Spot 7'),
--        (8, 38.74000, -9.15000, 8, 'Pickup Spot 8'),
--        (9, 38.74000, -9.15000, 9, 'Pickup Spot 9'),
--        (10, 38.74000, -9.15000, 10, 'Pickup Spot 10');

INSERT INTO liftdrop."item" (item_id, establishment, establishment_location, designation, price, ETA)
VALUES
    (1, 'MC DONALDS Roma', 1, 'Big Mac', 5.99, 1800),
    (2, 'BURGER KING MARQUÊS', 2, 'Whopper', 6.49, 2700),
    (3, 'MC DONALDS Saldanha', 3, 'McBifana', 5.99, 3000),
    (4, 'KFC COLOMBO', 4, 'Chick & Share 9 Tenders', 7.49, 3000),
    (5, 'TACO BELL VASCO DA GAMA', 5, 'Crunchwrap Supreme', 6.29, 2500),
    (6, 'SUBWAY AVENIDA DA LIBERDADE', 6, 'Turkey Sub', 6.89, 1200);


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

SELECT setval(pg_get_serial_sequence('liftdrop.request_declines', 'decline_id'), (SELECT MAX(decline_id) FROM liftdrop.request_declines));