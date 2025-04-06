-- Insert users first (no dependencies)
INSERT INTO Users (email, password, name)
VALUES
    ('john.doe@example.com', 'hashed_password_1', 'John Doe'),
    ('jane.smith@example.com', 'hashed_password_2', 'Jane Smith'),
    ('michael.jones@example.com', 'hashed_password_3', 'Michael Jones');

-- Insert locations next (no dependencies)
INSERT INTO Location (latitude, longitude, address, name)
VALUES
    (40.7128, -74.0060, 'New York, NY', 'New York City'),
    (34.0522, -118.2437, 'Los Angeles, CA', 'Los Angeles'),
    (37.7749, -122.4194, 'San Francisco, CA', 'San Francisco');

-- Insert clients (depends on Users)
INSERT INTO Clients (user_id, address)
VALUES
    (1, '123 Main St, New York, NY'),
    (2, '456 Oak St, Los Angeles, CA');

-- Insert couriers (depends on Users and Location)
INSERT INTO Couriers (user_id, current_location, is_available)
VALUES
    (3, NULL, TRUE);

-- Insert requests (depends on Clients and Couriers)
INSERT INTO Request (client_id, courier_id, created_at, request_status, ETA)
VALUES
    (1, 3, NOW(), 'PENDING', '00:30:00'),
    (2, 3, NOW(), 'ACCEPTED', '00:45:00');

-- Insert request details (depends on Request and Location)
INSERT INTO RequestDetails (request_id, description, pickup_location_id, dropoff_location_id)
VALUES
    (1, 'Pickup documents', 1, 2),
    (2, 'Deliver food package', 2, 3);