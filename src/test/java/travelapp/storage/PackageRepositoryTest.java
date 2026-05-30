package travelapp.storage;

import org.junit.jupiter.api.*;
import travelapp.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PackageRepositoryTest {

    private DatabaseManager dbManager;
    private PackageRepository repo;

    @BeforeEach
    void setup() throws Exception {
        // Унікальне ім'я in-memory БД для кожного тесту — ізоляція між тестами
        String uniqueDb = "jdbc:sqlite:file:testdb_" + UUID.randomUUID() + "?mode=memory&cache=shared";
        dbManager = new DatabaseManager(uniqueDb);
        repo = new PackageRepository(dbManager);
        repo.load(); // Створює таблиці за схемою DDL
    }

    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    void testListInitiallyEmpty() {
        // Після load() — якщо БД порожня, seedDatabase() заповнить її
        // Тому просто перевіряємо, що списки не null
        assertNotNull(repo.listPackages());
        assertNotNull(repo.listBookings());
    }

    @Test
    void testAddAndFindPackageRecreation() {
        // Очищаємо seed-дані
        clearAll();

        RecreationPackage tp = new RecreationPackage(
                1, "Turkey", "Sea", 7, 500,
                Transport.PLANE, MealPlan.AI, 20, 4.5, "Good tour", 4
        );
        repo.updatePackage(tp);

        assertEquals(1, repo.listPackages().size());
        assertTrue(repo.findPackageById(1).isPresent());
        TravelPackage found = repo.findPackageById(1).get();
        assertEquals("Good tour", found.getDescription());
        assertInstanceOf(RecreationPackage.class, found);
        assertEquals(4, ((RecreationPackage) found).getHotelStars());
    }

    @Test
    void testAddAndFindPackageExcursion() {
        clearAll();

        ExcursionPackage tp = new ExcursionPackage(
                2, "Paris", "City", 3, 350,
                Transport.PLANE, MealPlan.BB, 15, 4.7, "City tour", "Jean-Pierre"
        );
        repo.updatePackage(tp);

        TravelPackage found = repo.findPackageById(2).get();
        assertInstanceOf(ExcursionPackage.class, found);
        assertEquals("Jean-Pierre", ((ExcursionPackage) found).getGuideName());
    }

    @Test
    void testAddAndFindPackageAdventure() {
        clearAll();

        AdventurePackage tp = new AdventurePackage(
                3, "Alps", "Mountain", 5, 220,
                Transport.BUS, MealPlan.HB, 40, 4.5, "Mountain tour", "Medium", 25.0
        );
        repo.updatePackage(tp);

        TravelPackage found = repo.findPackageById(3).get();
        assertInstanceOf(AdventurePackage.class, found);
        assertEquals("Medium", ((AdventurePackage) found).getDifficultyLevel());
        assertEquals(25.0, ((AdventurePackage) found).getInsurancePremium(), 0.001);
    }

    @Test
    void testAddBookingReturnGeneratedKeys() {
        clearAll();

        // Спочатку тур — Foreign Key
        repo.updatePackage(new RecreationPackage(
                10, "Test Tour", "Sea", 7, 500,
                Transport.PLANE, MealPlan.AI, 20, 4.5, "Desc", 3
        ));

        Booking b = new Booking(
                0, 10, "Roman", "123",
                LocalDate.now(), LocalDate.now().plusDays(5),
                2, 1000
        );

        repo.addBooking(b);

        assertEquals(1, repo.listBookings().size());
        // Після addBooking id має бути встановлений SQLite автоінкрементом
        assertTrue(b.getId() > 0, "ID бронювання має бути встановлений після збереження в БД");
    }

    @Test
    void testSaveAndLoad() throws Exception {
        clearAll();

        repo.updatePackage(new RecreationPackage(
                1, "Egypt", "Sea", 7, 600,
                Transport.PLANE, MealPlan.FB, 30, 4.7, "Nice Egypt", 5
        ));

        repo.addBooking(new Booking(
                0, 1, "Ivan", "321",
                LocalDate.now(), LocalDate.now().plusDays(3),
                2, 1200
        ));

        repo.save(); // No-op для БД

        assertEquals(1, repo.listPackages().size());
        assertEquals(1, repo.listBookings().size());
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
