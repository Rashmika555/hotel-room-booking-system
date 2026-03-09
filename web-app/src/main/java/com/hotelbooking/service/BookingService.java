package com.hotelbooking.service;

import com.hotelbooking.entity.Booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(Booking booking);

    List<Booking> getAllBookings();

    Booking getBookingById(Integer id);

    Booking updateBooking(Integer id, Booking booking);

    void deleteBooking(Integer id);

    List<Booking> searchByCustomerName(String customerName);

    List<Booking> filterByStatus(String status);
}
