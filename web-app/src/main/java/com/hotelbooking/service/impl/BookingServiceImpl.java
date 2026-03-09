package com.hotelbooking.service.impl;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.exception.InvalidBookingException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.service.BookingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking createBooking(Booking booking) {
        validateMandatoryFields(booking);
        validateDates(booking.getCheckInDate(), booking.getCheckOutDate());
        validateAmount(booking.getBookingAmount());

        booking.setCustomerName(booking.getCustomerName().trim());
        booking.setRoomType(normalizeRoomType(booking.getRoomType()));
        booking.setBookingStatus("Booked");
        booking.setCreatedDateTime(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }

    @Override
    public Booking updateBooking(Integer id, Booking updatedBooking) {
        Booking existing = getBookingById(id);

        if (updatedBooking.getCustomerName() != null) {
            if (updatedBooking.getCustomerName().isBlank()) {
                throw new InvalidBookingException("Customer name cannot be empty.");
            }
            existing.setCustomerName(updatedBooking.getCustomerName().trim());
        }

        if (updatedBooking.getRoomNumber() != null) {
            if (updatedBooking.getRoomNumber() <= 0) {
                throw new InvalidBookingException("Room number must be greater than 0.");
            }
            existing.setRoomNumber(updatedBooking.getRoomNumber());
        }

        if (updatedBooking.getRoomType() != null && !updatedBooking.getRoomType().isBlank()) {
            existing.setRoomType(normalizeRoomType(updatedBooking.getRoomType()));
        }

        LocalDate effectiveCheckIn = updatedBooking.getCheckInDate() != null
                ? updatedBooking.getCheckInDate()
                : existing.getCheckInDate();

        LocalDate effectiveCheckOut = updatedBooking.getCheckOutDate() != null
                ? updatedBooking.getCheckOutDate()
                : existing.getCheckOutDate();

        validateDates(effectiveCheckIn, effectiveCheckOut);

        if (updatedBooking.getCheckInDate() != null) {
            existing.setCheckInDate(updatedBooking.getCheckInDate());
        }
        if (updatedBooking.getCheckOutDate() != null) {
            existing.setCheckOutDate(updatedBooking.getCheckOutDate());
        }

        if (updatedBooking.getBookingAmount() != null) {
            validateAmount(updatedBooking.getBookingAmount());
            existing.setBookingAmount(updatedBooking.getBookingAmount());
        }

        if (updatedBooking.getBookingStatus() != null && !updatedBooking.getBookingStatus().isBlank()) {
            existing.setBookingStatus(normalizeStatus(updatedBooking.getBookingStatus()));
        }

        return bookingRepository.save(existing);
    }

    @Override
    public void deleteBooking(Integer id) {
        Booking existing = getBookingById(id);
        bookingRepository.delete(existing);
    }

    @Override
    public List<Booking> searchByCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidBookingException("customerName query parameter is required.");
        }
        return bookingRepository.findByCustomerNameContainingIgnoreCase(customerName.trim());
    }

    @Override
    public List<Booking> filterByStatus(String status) {
        String normalizedStatus = normalizeStatus(status);
        return bookingRepository.findByBookingStatusIgnoreCase(normalizedStatus);
    }

    private void validateMandatoryFields(Booking booking) {
        if (booking == null) {
            throw new InvalidBookingException("Booking payload cannot be null.");
        }
        if (booking.getCustomerName() == null || booking.getCustomerName().trim().isEmpty()) {
            throw new InvalidBookingException("Customer name is mandatory.");
        }
        if (booking.getRoomNumber() == null || booking.getRoomNumber() <= 0) {
            throw new InvalidBookingException("Room number is mandatory and must be greater than 0.");
        }
        if (booking.getCheckInDate() == null) {
            throw new InvalidBookingException("Check-in date is mandatory.");
        }
    }

    private void validateDates(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null) {
            throw new InvalidBookingException("Check-in date is mandatory.");
        }
        if (checkOutDate != null && !checkOutDate.isAfter(checkInDate)) {
            throw new InvalidBookingException("Check-out date must be later than check-in date.");
        }
    }

    private void validateAmount(Double bookingAmount) {
        if (bookingAmount != null && bookingAmount < 0) {
            throw new InvalidBookingException("Booking amount cannot be negative.");
        }
    }

    private String normalizeRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return "Single";
        }

        String normalized = roomType.trim().toLowerCase();
        if (normalized.equals("single")) {
            return "Single";
        }
        if (normalized.equals("double")) {
            return "Double";
        }
        if (normalized.equals("suite")) {
            return "Suite";
        }

        throw new InvalidBookingException("Invalid roomType. Allowed values: Single, Double, Suite.");
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new InvalidBookingException("Booking status is required.");
        }

        String normalized = status.trim().toLowerCase();
        if (normalized.equals("booked")) {
            return "Booked";
        }
        if (normalized.equals("checked-in") || normalized.equals("checked in") || normalized.equals("checked_in")) {
            return "Checked-In";
        }
        if (normalized.equals("checked-out") || normalized.equals("checked out") || normalized.equals("checked_out")) {
            return "Checked-Out";
        }
        if (normalized.equals("cancelled") || normalized.equals("canceled")) {
            return "Cancelled";
        }

        throw new InvalidBookingException("Invalid bookingStatus. Allowed: Booked, Checked-In, Checked-Out, Cancelled.");
    }
}
