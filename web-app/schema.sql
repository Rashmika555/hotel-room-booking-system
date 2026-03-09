CREATE DATABASE IF NOT EXISTS hotel_booking_db;
USE hotel_booking_db;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    room_number INT NOT NULL,
    room_type VARCHAR(50),
    check_in_date DATE NOT NULL,
    check_out_date DATE,
    booking_amount DOUBLE,
    booking_status VARCHAR(50),
    created_datetime DATETIME
);
