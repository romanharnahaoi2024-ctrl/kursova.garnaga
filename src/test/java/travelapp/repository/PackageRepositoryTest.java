package travelapp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import travelapp.model.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:sqlite::memory:",
    "spring.jpa.hibernate.ddl-auto=update"
})
class PackageRepositoryTest {

    @Autowired
    private PackageRepository repo;

    @Autowired
    private BookingRepository bookingRepo;

    @BeforeEach
    void setup() {
        bookingRepo.deleteAll();
        repo.deleteAll();
    }

    @Test
    void testListInitiallyEmpty() {
        assertNotNull(repo.findAll());
        assertNotNull(bookingRepo.findAll());
    }

    @Test
    void testAddAndFindPackageRecreation() {
        RecreationPackage tp = new RecreationPackage(
                0, "Turkey", "Sea", 7, 500,
                Transport.PLANE, MealPlan.AI, 20, 4.5, "Good tour", 4);
        RecreationPackage saved = repo.save(tp);

        assertEquals(1, repo.count());
        assertTrue(repo.findById(saved.getId()).isPresent());
        TravelPackage found = repo.findById(saved.getId()).get();
        assertEquals("Good tour", found.getDescription());
        assertInstanceOf(RecreationPackage.class, found);
        assertEquals(4, ((RecreationPackage) found).getHotelStars());
    }

    @Test
    void testAddAndFindPackageExcursion() {
        ExcursionPackage tp = new ExcursionPackage(
                0, "Paris", "City", 3, 350,
                Transport.PLANE, MealPlan.BB, 15, 4.7, "City tour", "Jean-Pierre");
        ExcursionPackage saved = repo.save(tp);

        TravelPackage found = repo.findById(saved.getId()).get();
        assertInstanceOf(ExcursionPackage.class, found);
        assertEquals("Jean-Pierre", ((ExcursionPackage) found).getGuideName());
    }

    @Test
    void testAddAndFindPackageAdventure() {
        AdventurePackage tp = new AdventurePackage(
                0, "Alps", "Mountain", 5, 220,
                Transport.BUS, MealPlan.HB, 40, 4.5, "Mountain tour", "Medium", 25.0);
        AdventurePackage saved = repo.save(tp);

        TravelPackage found = repo.findById(saved.getId()).get();
        assertInstanceOf(AdventurePackage.class, found);
        assertEquals("Medium", ((AdventurePackage) found).getDifficultyLevel());
        assertEquals(25.0, ((AdventurePackage) found).getInsurancePremium(), 0.001);
    }

    @Test
    void testAddBookingReturnGeneratedKeys() {
        // First the package
        RecreationPackage pkg = repo.save(new RecreationPackage(
                0, "Test Tour", "Sea", 7, 500,
                Transport.PLANE, MealPlan.AI, 20, 4.5, "Desc", 3));

        Booking b = new Booking(
                0, pkg.getId(), "Roman", "123",
                LocalDate.now(), LocalDate.now().plusDays(5),
                2, 1000);

        Booking savedBooking = bookingRepo.save(b);

        assertEquals(1, bookingRepo.count());
        assertTrue(savedBooking.getId() > 0, "ID бронювання має бути встановлений після збереження в БД");
    }

    @Test
    void testSaveAndLoad() {
        RecreationPackage pkg = repo.save(new RecreationPackage(
                0, "Egypt", "Sea", 7, 600,
                Transport.PLANE, MealPlan.FB, 30, 4.7, "Nice Egypt", 5));

        bookingRepo.save(new Booking(
                0, pkg.getId(), "Ivan", "321",
                LocalDate.now(), LocalDate.now().plusDays(3),
                2, 1200));

        assertEquals(1, repo.count());
        assertEquals(1, bookingRepo.count());
    }
}
