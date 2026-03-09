import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    public enum RoomType {
        SINGLE("Single"),
        DOUBLE("Double"),
        SUITE("Suite");

        private final String label;

        RoomType(String label) {
            this.label = label;
        }

        public static RoomType fromString(String value) throws InvalidBookingException {
            if (value == null || value.trim().isEmpty()) {
                throw new InvalidBookingException("Room type is mandatory. Allowed values: Single, Double, Suite.");
            }

            String normalized = value.trim().replace("-", "_").replace(" ", "_").toUpperCase();
            try {
                return RoomType.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new InvalidBookingException("Invalid room type. Allowed values: Single, Double, Suite.");
            }
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum BookingStatus {
        BOOKED("Booked"),
        CHECKED_IN("Checked-In"),
        CHECKED_OUT("Checked-Out"),
        CANCELLED("Cancelled");

        private final String label;

        BookingStatus(String label) {
            this.label = label;
        }

        public static BookingStatus fromString(String value) throws InvalidBookingException {
            if (value == null || value.trim().isEmpty()) {
                throw new InvalidBookingException("Booking status is mandatory.");
            }

            String normalized = value.trim().replace("-", "_").replace(" ", "_").toUpperCase();
            try {
                return BookingStatus.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new InvalidBookingException("Invalid status. Allowed values: Booked, Checked-In, Checked-Out, Cancelled.");
            }
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final int bookingId;
    private String customerName;
    private int roomNumber;
    private RoomType roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double bookingAmount;
    private BookingStatus bookingStatus;
    private final LocalDateTime createdDateTime;

    public Booking(int bookingId, String customerName, int roomNumber, RoomType roomType,
                   LocalDate checkInDate, LocalDate checkOutDate, double bookingAmount) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingAmount = bookingAmount;
        this.bookingStatus = BookingStatus.BOOKED;
        this.createdDateTime = LocalDateTime.now();
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public double getBookingAmount() {
        return bookingAmount;
    }

    public void setBookingAmount(double bookingAmount) {
        this.bookingAmount = bookingAmount;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }
}
