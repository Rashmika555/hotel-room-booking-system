package com.hotelbooking.repository;

import com.hotelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByCustomerNameContainingIgnoreCase(String customerName);

    List<Booking> findByBookingStatusIgnoreCase(String bookingStatus);
}
