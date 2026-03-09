import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingService {
    private final List<Booking> bookings = new ArrayList<>();
    private final Map<Integer, Booking> bookingById = new HashMap<>();
    private int bookingSequence = 1;

    public Booking addBooking(String customerName, int roomNumber, String roomType,
                              LocalDate checkInDate, LocalDate checkOutDate,
                              double bookingAmount) throws InvalidBookingException {
        validateMandatoryFields(customerName, roomNumber, checkInDate);
        validateDates(checkInDate, checkOutDate);
        validateBookingAmount(bookingAmount);

        Booking newBooking = new Booking(
                bookingSequence++,
                customerName.trim(),
                roomNumber,
                Booking.RoomType.fromString(roomType),
                checkInDate,
                checkOutDate,
                bookingAmount
        );

        bookings.add(newBooking);
        bookingById.put(newBooking.getBookingId(), newBooking);
        return newBooking;
    }

    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public Booking getBookingById(int bookingId) throws InvalidBookingException {
        Booking booking = bookingById.get(bookingId);
        if (booking == null) {
            throw new InvalidBookingException("Booking not found for ID: " + bookingId);
        }
        return booking;
    }

    public Booking updateBooking(int bookingId, String status,
                                 LocalDate checkInDate, LocalDate checkOutDate,
                                 Double bookingAmount) throws InvalidBookingException {
        Booking booking = getBookingById(bookingId);

        LocalDate effectiveCheckIn = (checkInDate != null) ? checkInDate : booking.getCheckInDate();
        LocalDate effectiveCheckOut = (checkOutDate != null) ? checkOutDate : booking.getCheckOutDate();
        validateDates(effectiveCheckIn, effectiveCheckOut);

        if (checkInDate != null) {
            booking.setCheckInDate(checkInDate);
        }
        if (checkOutDate != null) {
            booking.setCheckOutDate(checkOutDate);
        }
        if (bookingAmount != null) {
            validateBookingAmount(bookingAmount);
            booking.setBookingAmount(bookingAmount);
        }
        if (status != null && !status.trim().isEmpty()) {
            booking.setBookingStatus(Booking.BookingStatus.fromString(status));
        }

        return booking;
    }

    public boolean deleteBookingById(int bookingId) {
        Booking booking = bookingById.remove(bookingId);
        if (booking == null) {
            return false;
        }
        bookings.remove(booking);
        return true;
    }

    public List<Booking> searchByCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return List.of();
        }

        String query = customerName.trim().toLowerCase();
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getCustomerName().toLowerCase().contains(query)) {
                result.add(booking);
            }
        }
        return result;
    }

    public List<Booking> filterByStatus(String status) throws InvalidBookingException {
        Booking.BookingStatus targetStatus = Booking.BookingStatus.fromString(status);
        List<Booking> result = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getBookingStatus() == targetStatus) {
                result.add(booking);
            }
        }

        return result;
    }

    private void validateMandatoryFields(String customerName, int roomNumber, LocalDate checkInDate)
            throws InvalidBookingException {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidBookingException("Customer name is mandatory and cannot be empty.");
        }
        if (roomNumber <= 0) {
            throw new InvalidBookingException("Room number is mandatory and must be greater than 0.");
        }
        if (checkInDate == null) {
            throw new InvalidBookingException("Check-in date is mandatory.");
        }
    }

    private void validateDates(LocalDate checkInDate, LocalDate checkOutDate) throws InvalidBookingException {
        if (checkInDate == null) {
            throw new InvalidBookingException("Check-in date is mandatory.");
        }
        if (checkOutDate != null && !checkOutDate.isAfter(checkInDate)) {
            throw new InvalidBookingException("Check-out date must be later than check-in date.");
        }
    }

    private void validateBookingAmount(double bookingAmount) throws InvalidBookingException {
        if (bookingAmount < 0) {
            throw new InvalidBookingException("Booking amount cannot be negative.");
        }
    }
}
