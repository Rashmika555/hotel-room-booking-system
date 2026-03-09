import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class BookingApp {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        BookingService bookingService = new BookingService();
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt(scanner, "Choose an option: ");

            try {
                switch (choice) {
                    case 1:
                        addBooking(scanner, bookingService);
                        break;
                    case 2:
                        printBookings(bookingService.getAllBookings());
                        break;
                    case 3:
                        updateBooking(scanner, bookingService);
                        break;
                    case 4:
                        deleteBooking(scanner, bookingService);
                        break;
                    case 5:
                        searchBookings(scanner, bookingService);
                        break;
                    case 6:
                        filterBookings(scanner, bookingService);
                        break;
                    case 0:
                        running = false;
                        System.out.println("Exiting application.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select a valid menu option.");
                }
            } catch (InvalidBookingException ex) {
                System.out.println("Validation Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Unexpected Error: " + ex.getMessage());
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n===== Hotel Booking Console Menu =====");
        System.out.println("1. Add Booking");
        System.out.println("2. View All Bookings");
        System.out.println("3. Update Booking");
        System.out.println("4. Delete Booking by ID");
        System.out.println("5. Search Bookings by Customer Name");
        System.out.println("6. Filter Bookings by Status");
        System.out.println("0. Exit");
    }

    private static void addBooking(Scanner scanner, BookingService bookingService) throws InvalidBookingException {
        String customerName = readString(scanner, "Customer Name: ");
        int roomNumber = readInt(scanner, "Room Number: ");
        String roomType = readString(scanner, "Room Type (Single/Double/Suite): ");
        LocalDate checkInDate = readDate(scanner, "Check-In Date (yyyy-MM-dd): ", true);
        LocalDate checkOutDate = readDate(scanner, "Check-Out Date (yyyy-MM-dd, optional): ", false);
        double bookingAmount = readDouble(scanner, "Booking Amount: ");

        Booking booking = bookingService.addBooking(
                customerName,
                roomNumber,
                roomType,
                checkInDate,
                checkOutDate,
                bookingAmount
        );

        System.out.println("Booking created successfully. Booking ID: " + booking.getBookingId());
    }

    private static void updateBooking(Scanner scanner, BookingService bookingService) throws InvalidBookingException {
        int bookingId = readInt(scanner, "Booking ID to update: ");
        Booking existing = bookingService.getBookingById(bookingId);

        System.out.println("Leave field blank to keep current value.");
        String statusInput = readStringAllowEmpty(scanner,
                "Status (Booked/Checked-In/Checked-Out/Cancelled) [Current: "
                        + existing.getBookingStatus() + "]: ");

        LocalDate checkInDate = readDateAllowEmpty(scanner,
                "Check-In Date (yyyy-MM-dd) [Current: " + existing.getCheckInDate() + "]: ");
        LocalDate checkOutDate = readDateAllowEmpty(scanner,
                "Check-Out Date (yyyy-MM-dd) [Current: " + existing.getCheckOutDate() + "]: ");

        Double bookingAmount = readDoubleAllowEmpty(scanner,
                "Booking Amount [Current: " + existing.getBookingAmount() + "]: ");

        Booking updated = bookingService.updateBooking(
                bookingId,
                statusInput,
                checkInDate,
                checkOutDate,
                bookingAmount
        );

        System.out.println("Booking updated successfully. Current Status: " + updated.getBookingStatus());
    }

    private static void deleteBooking(Scanner scanner, BookingService bookingService) {
        int bookingId = readInt(scanner, "Booking ID to delete: ");
        boolean deleted = bookingService.deleteBookingById(bookingId);

        if (deleted) {
            System.out.println("Booking deleted successfully.");
        } else {
            System.out.println("No booking found for ID: " + bookingId);
        }
    }

    private static void searchBookings(Scanner scanner, BookingService bookingService) {
        String customerName = readString(scanner, "Enter customer name to search: ");
        List<Booking> result = bookingService.searchByCustomerName(customerName);
        printBookings(result);
    }

    private static void filterBookings(Scanner scanner, BookingService bookingService) throws InvalidBookingException {
        String status = readString(scanner, "Enter status (Booked/Checked-In/Checked-Out/Cancelled): ");
        List<Booking> result = bookingService.filterByStatus(status);
        printBookings(result);
    }

    private static void printBookings(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        System.out.println("\n--------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s %-20s %-10s %-10s %-12s %-12s %-12s %-15s %-20s%n",
                "ID", "Customer", "Room No", "Type", "Check-In", "Check-Out", "Amount", "Status", "Created DateTime");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");

        for (Booking booking : bookings) {
            String checkOut = (booking.getCheckOutDate() == null) ? "-" : booking.getCheckOutDate().format(DATE_FORMAT);
            System.out.printf("%-5d %-20s %-10d %-10s %-12s %-12s %-12.2f %-15s %-20s%n",
                    booking.getBookingId(),
                    booking.getCustomerName(),
                    booking.getRoomNumber(),
                    booking.getRoomType(),
                    booking.getCheckInDate().format(DATE_FORMAT),
                    checkOut,
                    booking.getBookingAmount(),
                    booking.getBookingStatus(),
                    booking.getCreatedDateTime().format(DATE_TIME_FORMAT)
            );
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid amount. Please enter a valid decimal value.");
            }
        }
    }

    private static Double readDoubleAllowEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) {
                return null;
            }
            try {
                return Double.parseDouble(input.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid amount. Please enter a valid decimal value.");
            }
        }
    }

    private static String readString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (!input.trim().isEmpty()) {
                return input;
            }
            System.out.println("Value cannot be empty.");
        }
    }

    private static String readStringAllowEmpty(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static LocalDate readDate(Scanner scanner, String prompt, boolean mandatory) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty() && !mandatory) {
                return null;
            }

            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }

    private static LocalDate readDateAllowEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }
}
