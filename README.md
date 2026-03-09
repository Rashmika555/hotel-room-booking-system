# Hotel Booking System (Console + Full Stack Web)

This project contains two complete implementations:

- `console-app/` -> Core Java menu-driven booking manager
- `web-app/` -> Spring Boot + MySQL + REST + Bootstrap dashboard

## 1. Project Structure

```text
hotel-booking-system/
|-- console-app/
|   |-- Booking.java
|   |-- BookingService.java
|   |-- InvalidBookingException.java
|   `-- BookingApp.java
|
`-- web-app/
    |-- pom.xml
    |-- schema.sql
    `-- src/main/
        |-- java/com/hotelbooking/
        |   |-- controller/BookingController.java
        |   |-- entity/Booking.java
        |   |-- exception/
        |   |   |-- GlobalExceptionHandler.java
        |   |   |-- InvalidBookingException.java
        |   |   `-- ResourceNotFoundException.java
        |   |-- repository/BookingRepository.java
        |   |-- service/BookingService.java
        |   |-- service/impl/BookingServiceImpl.java
        |   `-- HotelBookingApplication.java
        `-- resources/
            |-- application.properties
            `-- static/
                |-- index.html
                |-- css/style.css
                `-- js/app.js
```

## 2. Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8+

## 3. Run Console Application

From `hotel-booking-system/console-app`:

```bash
javac *.java
java BookingApp
```

Menu options provided:

1. Add booking
2. View all bookings
3. Update booking
4. Delete booking by ID
5. Search by customer name
6. Filter by status
7. Exit

## 4. Setup MySQL Database

Option A (manual in MySQL client):

```sql
SOURCE /absolute/path/to/hotel-booking-system/web-app/schema.sql;
```

Option B:

- Start MySQL server
- Run `web-app/schema.sql` once to create schema
- Keep `spring.sql.init.mode=never` and let Hibernate handle future table updates

## 5. Configure DB Credentials

Edit `web-app/src/main/resources/application.properties` if needed:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

## 6. Run Web Application

From `hotel-booking-system/web-app`:

```bash
mvn clean spring-boot:run
```

Open:

- `http://localhost:8080` -> dashboard UI
- `http://localhost:8080/api/bookings` -> REST API list

## 7. REST API Endpoints

- `POST /api/bookings`
- `GET /api/bookings`
- `GET /api/bookings/{id}`
- `PUT /api/bookings/{id}`
- `DELETE /api/bookings/{id}`
- `GET /api/bookings/search?customerName={name}`
- `GET /api/bookings/status?status={status}`

## 8. Sample JSON (POST/PUT)

```json
{
	"customerName": "Alex Martin",
	"roomNumber": 204,
	"roomType": "Suite",
	"checkInDate": "2026-03-10",
	"checkOutDate": "2026-03-15",
	"bookingAmount": 8900.5,
	"bookingStatus": "Checked-In"
}
```

Notes:

- `bookingStatus` is optional on `POST`; backend sets default `Booked`
- `createdDateTime` is auto-generated
