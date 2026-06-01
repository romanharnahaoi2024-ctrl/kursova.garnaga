package travelapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TravelPackageTest {

        @Test
        void testGettersAndSetters() {
                // TravelPackage тепер абстрактний → використовуємо конкретний підклас
                RecreationPackage tp = new RecreationPackage(
                                1, "Turkey", "Sea",
                                7, 499.99,
                                Transport.PLANE, MealPlan.AI,
                                20, 4.8, 5);

                assertEquals(1, tp.getId());
                assertEquals("Turkey", tp.getName());
                assertEquals("Sea", tp.getType());
                assertEquals(7, tp.getDurationDays());
                assertEquals(499.99, tp.getBasePrice());
                assertEquals(Transport.PLANE, tp.getTransport());
                assertEquals(MealPlan.AI, tp.getMealPlan());
                assertEquals(20, tp.getAvailableSeats());
                assertEquals(4.8, tp.getRating());
                assertEquals(5, tp.getHotelStars());

                tp.setAvailableSeats(15);
                assertEquals(15, tp.getAvailableSeats());

                // Перевірка конструктора з description
                AdventurePackage tp2 = new AdventurePackage(
                                2, "Alps", "Mountain",
                                5, 200.0,
                                Transport.BUS, MealPlan.HB,
                                30, 4.5, "Beautiful mountain tour", "Medium", 25.0);
                assertEquals("Beautiful mountain tour", tp2.getDescription());
                tp2.setDescription("New description");
                assertEquals("New description", tp2.getDescription());
        }

        @Test
        void testToString() {
                RecreationPackage tp = new RecreationPackage(
                                2, "Egypt", "Sea",
                                10, 700,
                                Transport.PLANE, MealPlan.FB,
                                30, 4.6, 4);

                String s = tp.toString();
                System.out.println("TOSTRING = " + s);

                assertTrue(s.contains("Egypt"));
                assertTrue(s.contains("Sea"));
                assertTrue(s.contains("10"));
                assertTrue(s.contains("PLANE"));
                assertTrue(s.contains("FB"));
        }

        @Test
        void testPolymorphicPriceCalculation() {
                RecreationPackage recreation = new RecreationPackage(
                                1, "Sea Tour", "Sea", 7, 500.0, Transport.PLANE, MealPlan.AI, 20, 4.5, 4);
                // (500 + 4*5) * 2 = 520 * 2 = 1040
                assertEquals(1040.0, recreation.calculateFinalPrice(2), 0.001);

                ExcursionPackage excursion = new ExcursionPackage(
                                2, "City Tour", "City", 3, 300.0, Transport.BUS, MealPlan.BB, 15, 4.7, "Guide A");
                // (300 + 15) * 3 = 315 * 3 = 945
                assertEquals(945.0, excursion.calculateFinalPrice(3), 0.001);

                AdventurePackage adventure = new AdventurePackage(
                                3, "Mountain Tour", "Mountain", 5, 200.0, Transport.BUS, MealPlan.HB, 30, 4.5, "Medium",
                                30.0);
                // (200 + 30) * 2 = 230 * 2 = 460
                assertEquals(460.0, adventure.calculateFinalPrice(2), 0.001);
        }

        @Test
        void testSetId() {
                RecreationPackage tp = new RecreationPackage(
                                0, "Test", "Sea", 5, 100.0, Transport.PLANE, MealPlan.AI, 10, 4.0, 3);
                tp.setId(42);
                assertEquals(42, tp.getId());
        }
}
