-- Insert users first (no dependencies)
INSERT INTO liftdrop."user" (email, password, name, role)
VALUES ('john.doe@example.com', 'hashed_password_1', 'John Doe', 'CLIENT'),
       ('jane.smith@example.com', 'hashed_password_2', 'Jane Smith', 'CLIENT'),
       ('michael.jones@example.com', 'hashed_password_3', 'Michael Jones', 'CLIENT'),
       ('john.doe2@example.com', 'hashed_password_4', 'John Doe2', 'COURIER'),
       ('jane.smith2@example.com', 'hashed_password_5', 'Jane Smith2', 'COURIER'),
       ('michael.jones2@example.com', 'hashed_password_6', 'Michael Jones2', 'COURIER'),
       ('courier7@example.com', 'hashed_password_7', 'Courier Seven', 'COURIER'),
       ('courier8@example.com', 'hashed_password_8', 'Courier Eight', 'COURIER'),
       ('courier9@example.com', 'hashed_password_9', 'Courier Nine', 'COURIER'),
       ('courier10@example.com', 'hashed_password_10', 'Courier Ten', 'COURIER');

-- Insert Addresses
INSERT INTO liftdrop."address" (country, city, street, house_number, floor, zip_code)
VALUES ('PORTUGAL', 'Lisbon', 'Avenida de Roma', '15', 'RC', '1000-264'),
       ('PORTUGAL', 'Lisbon', 'Avenida Fontes Pereira de Melo', '16', 'RC', '1050-116'),
       ('PORTUGAL', 'Lisbon', 'Avenida da República', '12', '1', '1050-191'),
       ('PORTUGAL', 'Lisbon', 'Rua Albert Einstein', '1', '2', '1500-676'),
       ('PORTUGAL', 'Lisbon', 'Avenida Dom João II', '40', '3', '1990-094'),
       ('PORTUGAL', 'Lisbon', 'Avenida da Liberdade', '2', 'RC', '1250-113'),
       ('PORTUGAL', 'Odivelas', 'Av. Prof. Dr. Augusto Abreu Lopes', '2', 'RC', '2675-462');

-- Insert locations next (no dependencies)
INSERT INTO liftdrop."location" (location_id, latitude, longitude, address, name)
VALUES (1, 38.743424, -9.138986, 1, 'Avenida de Roma'),
       (2, 38.7267130611275, -9.148917466507026, 2, 'Avenida Fontes Pereira de Melo'),
       (3, 38.73548667805665, -9.144861015848331, 3, 'Avenida da Republica'),
       (4, 38.75504773230685, -9.188669486292124 , 4, 'Centro Colombo'),
       (5, 38.768230407427936, -9.09637567868234, 5, 'Centro Comercial Vasco da Gama'),
       (6, 38.716107488039675, -9.142992520654982, 6, 'Avenida da Liberdade'),
       (7, 38.794485074412876, -9.181222211849477, 7, 'McDonalds Odivelas');

-- Insert clients (depends on Users)
INSERT INTO liftdrop."client" (client_id, address)
VALUES (1, 1),
       (2, 2),
       (3, 3);

-- Insert couriers (depends on Users and Location)
INSERT INTO liftdrop."courier" (courier_id, current_location, rating, daily_earnings, is_available)
VALUES (4, 1, 1.0, 0.00, true),
       (5, 2, 1.0, 0.00, false),
       (6, 3, 1.0, 0.00, false),
       (7, 4, 1.0, 0.00, false),
       (8, 5, 1.0, 0.00, true),
       (9, 6, 1.0, 0.00, false),
       (10, 4, 1.0, 0.00, false);

-- Insert requests (depends on Clients and Couriers)
INSERT INTO liftdrop."request" (request_id, client_id, courier_id, created_at, request_status, ETA, pickup_code, dropoff_code)
VALUES (1, 1, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 1800, '123456', '654321'),
       (2, 2, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 2700, '789012', '210987'),
       (3, 3, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 3600, '345678', '876543'),
       (4, 1, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 1800, '901234', '432109'),
       (5, 2, NULL, EXTRACT(EPOCH FROM NOW()), 'PENDING', 2700, '567890', '098765');

-- Insert request details (depends on Request and Location)
INSERT INTO liftdrop."request_details" (request_id, description, quantity, pickup_location, dropoff_location)
VALUES (1, 'Chicken Wings', 1, 1, 2),
       (2, 'Big Mac', 1, 2, 3);

-- Insert items
INSERT INTO liftdrop."item" (item_id, establishment, establishment_location, designation, price, ETA)
VALUES
    (1, 'MC DONALDS Roma', 1, 'Big Mac', 5.99, 1800),
    (2, 'BURGER KING MARQUÊS', 2, 'Whopper', 6.49, 2700),
    (3, 'MC DONALDS Saldanha', 3, 'Big Mac', 5.99, 2000),
    (4, 'KFC COLOMBO', 4, 'Chick & Share 9 Tenders', 7.49, 3000),
    (5, 'TACO BELL VASCO DA GAMA', 5, 'Crunchwrap Supreme', 6.29, 2500),
    (6, 'SUBWAY AVENIDA DA LIBERDADE', 6, 'Turkey Sub', 6.89, 1200),
    (7, 'MC DONALDS Odivelas', 7, 'Big Mac', 5.99, 1800);

-- Insert deliveries
INSERT INTO liftdrop."delivery" (delivery_id, courier_id, request_id, started_at, completed_at, ETA, delivery_status)
VALUES (1, 4, 3, EXTRACT(EPOCH FROM NOW()), EXTRACT(EPOCH FROM NOW()), 1800, 'IN_PROGRESS');

-- Insert courier ratings (new table)
INSERT INTO liftdrop."courier_rating" (courier_id, request_id, client_id, rating, comment)
VALUES (4, 3, 3, 4.5, 'Great delivery!'),
       (5, 2, 2, 5.0, 'Very fast and polite.'),
       (6, 1, 1, 3.5, 'Average experience.');

-- Update sequences
SELECT setval(pg_get_serial_sequence('liftdrop.user', 'user_id'), (SELECT MAX(user_id) FROM liftdrop.user));
SELECT setval(pg_get_serial_sequence('liftdrop.address', 'address_id'), (SELECT MAX(address_id) FROM liftdrop.address));
SELECT setval(pg_get_serial_sequence('liftdrop.location', 'location_id'), (SELECT MAX(location_id) FROM liftdrop.location));
SELECT setval(pg_get_serial_sequence('liftdrop.request', 'request_id'), (SELECT MAX(request_id) FROM liftdrop.request));
SELECT setval(pg_get_serial_sequence('liftdrop.delivery', 'delivery_id'), (SELECT MAX(delivery_id) FROM liftdrop.delivery));
SELECT setval(pg_get_serial_sequence('liftdrop.request_declines', 'decline_id'), (SELECT MAX(decline_id) FROM liftdrop.request_declines));