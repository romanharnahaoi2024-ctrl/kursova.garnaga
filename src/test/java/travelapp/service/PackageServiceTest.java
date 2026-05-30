package travelapp.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import travelapp.*;
import travelapp.storage.DatabaseManager;
import travelapp.storage.PackageRepository;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PackageServiceTest {

    private DatabaseManager dbManager;
    private PackageRepository repo;
    private PackageService service;

    @BeforeEach
    void setup() throws Exception {
        // Унікальне ім'я in-memory БД для кожного тесту — ізоляція між тестами
        String uniqueDb = "jdbc:sqlite:file:svctest_" + UUID.randomUUID() + "?mode=memory&cache=shared";
        dbManager = new DatabaseManager(uniqueDb);
        repo = new PackageRepository(dbManager);
        repo.load(); // Ініціалізує схему та виконує seed

        // Очищаємо seed-дані щоб тест мав повний контроль над вмістом БД
        clearAll();
        service = new PackageService(repo);

        // Тестовий тур: RecreationPackage (Sea), hotelStars=4
        // calculateFinalPrice(seats) = (500 + 4*5) * seats = 520 * seats
        repo.updatePackage(new RecreationPackage(
                1, "Turkey", "Sea", 7, 500,
                Transport.PLANE, MealPlan.AI, 20, 4.5, 4
        ));
    }

    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    void testFilterWorks() {
        var list = service.filter("Sea", Transport.PLANE, MealPlan.AI, 0, 0);
        assertEquals(1, list.size());
    }

    @Test
    void testSortByPrice() {
        repo.updatePackage(new RecreationPackage(
                2, "Egypt", "Sea", 10, 300,
                Transport.PLANE, MealPlan.AI, 20, 4.7, 3
        ));

        var sorted = service.sortBy("price");
        assertEquals(300, sorted.get(0).getBasePrice());
    }

    @Test
    void testBookSuccess() {
        Booking b = service.book(
                1, "Roman", "111",
                LocalDate.now(), LocalDate.now().plusDays(7),
                2
        );

        // RecreationPackage: (500 + 4*5) * 2 = 520 * 2 = 1040
        assertEquals(1040.0, b.getTotalPrice(), 0.001);
        assertEquals(18, repo.findPackageById(1).get().getAvailableSeats());
        assertEquals(1, repo.listBookings().size());
        // Перевіряємо, що SQLite встановив автоінкрементний id
        assertTrue(b.getId() > 0, "ID бронювання має бути > 0 після збереження");
    }

    @Test
    void testBookFailsIfNotEnoughSeats() {
        assertThrows(IllegalArgumentException.class, () ->
                service.book(1, "Test", "000",
                        LocalDate.now(), LocalDate.now(), 999)
        );
    }

    @Test
    void testBookFailsIfPackageNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                service.book(999, "Test", "000",
                        LocalDate.now(), LocalDate.now(), 2)
        );
    }

    @Test
    void testSaveOrUpdate() {
        // Create new
        TravelPackage newPkg = new RecreationPackage(
                0, "New Tour", "Sea", 5, 200,
                Transport.BUS, MealPlan.BB, 10, 4.0, 3
        );
        service.saveOrUpdate(newPkg);
        assertEquals(2, repo.listPackages().size());

        // Update existing
        TravelPackage existingPkg = repo.findPackageById(1).get();
        existingPkg.setDescription("Updated Turkey");
        service.saveOrUpdate(existingPkg);
        assertEquals("Updated Turkey", repo.findPackageById(1).get().getDescription());
    }

    private void clearAll() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM bookings");
            stmt.execute("DELETE FROM travel_packages");
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear tables", e);
        }
    }
}
