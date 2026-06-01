package travelapp.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testConstructorAndGetters() {
        LocalDate start = LocalDate.of(2025, 1, 10);
        LocalDate end = LocalDate.of(2025, 1, 20);

        Booking b = new Booking(
                1, 10, "Roman", "123",
                start, end, 3, 1500.0
        );

        assertEquals(1, b.getId());
        assertEquals(10, b.getPackageId());
        assertEquals("Roman", b.getCustomerName());
        assertEquals("123", b.getContactInfo());
        assertEquals(start, b.getStartDate());
        assertEquals(end, b.getEndDate());
        assertEquals(3, b.getSeatsBooked());
        assertEquals(1500.0, b.getTotalPrice());
    }

    @Test
    void testToString() {
        Booking b = new Booking(
                1, 5, "Oleh", "321",
                LocalDate.now(), LocalDate.now().plusDays(3),
                2, 500
        );

        assertTrue(b.toString().contains("Booking #1"));
        assertTrue(b.toString().contains("seats=2"));
    }
}
