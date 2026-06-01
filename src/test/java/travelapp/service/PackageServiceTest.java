package travelapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import travelapp.model.*;
import travelapp.repository.BookingRepository;
import travelapp.repository.PackageRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:sqlite::memory:",
    "spring.jpa.hibernate.ddl-auto=update"
})
class PackageServiceTest {

    @Autowired
    private PackageRepository repo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private PackageService service;

    @BeforeEach
    void setup() throws Exception {
        // Clear tables to have full control over the database content
        bookingRepo.deleteAll();
        repo.deleteAll();

        // Test package: RecreationPackage (Sea), hotelStars=4
        repo.save(new RecreationPackage(
                0, "Turkey", "Sea", 7, 500,
                Transport.PLANE, MealPlan.AI, 20, 4.5, 4));
    }

    @Test
    void testFilterWorks() {
        var list = service.filter("Sea", Transport.PLANE, MealPlan.AI, 0, 0);
        assertEquals(1, list.size());
    }

    @Test
    void testSortByPrice() {
        repo.save(new RecreationPackage(
                0, "Egypt", "Sea", 10, 300,
                Transport.PLANE, MealPlan.AI, 20, 4.7, 3));

        var sorted = service.sortBy("price");
        assertEquals(300, sorted.get(0).getBasePrice());
    }

    @Test
    void testBookSuccess() {
        int pkgId = repo.findAll().get(0).getId();
        Booking b = service.book(
                pkgId, "Roman", "111",
                LocalDate.now(), LocalDate.now().plusDays(7),
                2);

        // RecreationPackage: (500 + 4*5) * 2 = 520 * 2 = 1040
        assertEquals(1040.0, b.getTotalPrice(), 0.001);
        assertEquals(18, repo.findById(pkgId).get().getAvailableSeats());
        assertEquals(1, bookingRepo.count());
        assertTrue(b.getId() > 0, "ID бронювання має бути > 0 після збереження");
    }

    @Test
    void testBookFailsIfNotEnoughSeats() {
        int pkgId = repo.findAll().get(0).getId();
        assertThrows(IllegalArgumentException.class, () -> service.book(pkgId, "Test", "000",
                LocalDate.now(), LocalDate.now(), 999));
    }

    @Test
    void testBookFailsIfPackageNotFound() {
        assertThrows(IllegalArgumentException.class, () -> service.book(9999, "Test", "000",
                LocalDate.now(), LocalDate.now(), 2));
    }

    @Test
    void testSaveOrUpdate() {
        // Create new
        TravelPackage newPkg = new RecreationPackage(
                0, "New Tour", "Sea", 5, 200,
                Transport.BUS, MealPlan.BB, 10, 4.0, 3);
        service.saveOrUpdate(newPkg);
        assertEquals(2, repo.count());

        // Update existing
        TravelPackage existingPkg = repo.findAll().get(0);
        existingPkg.setDescription("Updated Turkey");
        service.saveOrUpdate(existingPkg);
        assertEquals("Updated Turkey", repo.findById(existingPkg.getId()).get().getDescription());
    }
}
