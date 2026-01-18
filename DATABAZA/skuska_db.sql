-- ================================
-- Databáza
-- ================================
CREATE DATABASE IF NOT EXISTS hotel_reservations
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE hotel_reservations;

-- ================================
-- Izby
-- ================================
CREATE TABLE room (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    price_per_night DECIMAL(8,2) NOT NULL,
    capacity INT NOT NULL
);

INSERT INTO room (room_number, type, price_per_night, capacity) VALUES
('101', 'Single', 50.00, 1),
('102', 'Double', 80.00, 2),
('103', 'Double', 85.00, 2),
('201', 'Suite', 150.00, 4),
('202', 'Suite', 180.00, 5);

-- ================================
-- Hostia (iba zodpovedná osoba)
-- ================================
CREATE TABLE guest (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(30)
);

INSERT INTO guest (first_name, last_name, email, phone) VALUES
('Ján', 'Novák', 'jan.novak@email.sk', '0900123456'),
('Petra', 'Malá', 'petra.mala@email.sk', '0911222333'),
('Martin', 'Hrivnák', 'martin.hrivnak@email.sk', '0900555666'),
('Lucia', 'Kováčová', 'lucia.kovacova@email.sk', '0911444555'),
('Tomáš', 'Bielik', 'tomas.bielik@email.sk', '0900777888');

-- ================================
-- Služby
-- ================================
CREATE TABLE service (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    price DECIMAL(8,2) NOT NULL
);

INSERT INTO service (name, price) VALUES
('Raňajky', 10.00),
('Parkovanie', 5.00),
('Wellness', 20.00),
('Detská postieľka', 8.00);

-- ================================
-- Rezervácie (zodpovedná osoba + počet hostí)
-- ================================
CREATE TABLE reservation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    responsible_guest_id INT NOT NULL,
    guest_count INT NOT NULL DEFAULT 1,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    status ENUM('CREATED', 'CONFIRMED', 'CANCELLED') DEFAULT 'CREATED',

    FOREIGN KEY (room_id) REFERENCES room(id),
    FOREIGN KEY (responsible_guest_id) REFERENCES guest(id),
    CHECK (date_from < date_to)
);

INSERT INTO reservation (room_id, responsible_guest_id, guest_count, date_from, date_to, status) VALUES
(1, 1, 1, '2026-01-10', '2026-01-12', 'CONFIRMED'),
(2, 2, 2, '2026-01-11', '2026-01-15', 'CONFIRMED'),
(4, 3, 3, '2026-02-01', '2026-02-05', 'CREATED'),
(5, 4, 2, '2026-02-10', '2026-02-12', 'CREATED');

-- ================================
-- Rezervácia ↔ Služby
-- ================================
CREATE TABLE reservation_service (
    reservation_id INT NOT NULL,
    service_id INT NOT NULL,
    quantity INT DEFAULT 1,
    PRIMARY KEY (reservation_id, service_id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES service(id)
);

INSERT INTO reservation_service (reservation_id, service_id, quantity) VALUES
-- Rezervácia 1
(1, 1, 2),
(1, 2, 1),

-- Rezervácia 2
(2, 1, 2),
(2, 3, 1),

-- Rezervácia 3
(3, 1, 3),
(3, 4, 1),

-- Rezervácia 4
(4, 2, 1),
(4, 3, 1);
