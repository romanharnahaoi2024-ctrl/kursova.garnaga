package travelapp.storage;

import travelapp.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PackageRepository {
    private final DatabaseManager dbManager;

    public PackageRepository(String packagesFile, String bookingsFile) {
        this(new DatabaseManager("jdbc:sqlite:data/travel.db"));
    }

    public PackageRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void load() throws Exception {
        dbManager.initializeDatabase();
        if (loadAllPackages().isEmpty()) {
            seedDatabase();
        }
    }

    public void save() {
        // No-op для бази даних — зміни зберігаються автоматично при кожній транзакції
    }

    // -------------------------------------------------------------------------
    // CRUD: TravelPackage
    // -------------------------------------------------------------------------

    public List<TravelPackage> loadAllPackages() {
        List<TravelPackage> list = new ArrayList<>();
        String sql = "SELECT id, name, type, duration_days, base_price, transport, meal_plan, " +
                     "available_seats, rating, description, hotel_stars, guide_name, " +
                     "difficulty_level, insurance_premium FROM travel_packages";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToPackage(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading packages", e);
        }
        return list;
    }

    public List<TravelPackage> listPackages() {
        return Collections.unmodifiableList(loadAllPackages());
    }

    public Optional<TravelPackage> findPackageById(int id) {
        String sql = "SELECT id, name, type, duration_days, base_price, transport, meal_plan, " +
                     "available_seats, rating, description, hotel_stars, guide_name, " +
                     "difficulty_level, insurance_premium FROM travel_packages WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPackage(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding package by id=" + id, e);
        }
        return Optional.empty();
    }

    /**
     * Вставляє або оновлює тур залежно від того, чи існує запис з таким id.
     * Після вставки без id — встановлює згенерований БД id у об'єкт.
     */
    public void updatePackage(TravelPackage tp) {
        boolean exists = false;
        if (tp.getId() > 0) {
            String check = "SELECT 1 FROM travel_packages WHERE id = ?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(check)) {
                pstmt.setInt(1, tp.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    exists = rs.next();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error checking package existence", e);
            }
        }

        if (exists) {
            String sql = "UPDATE travel_packages SET name=?, type=?, duration_days=?, base_price=?, " +
                         "transport=?, meal_plan=?, available_seats=?, rating=?, description=?, " +
                         "hotel_stars=?, guide_name=?, difficulty_level=?, insurance_premium=? WHERE id=?";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                fillCommonParams(pstmt, tp);
                pstmt.setInt(14, tp.getId());
                pstmt.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException("Error updating package id=" + tp.getId(), e);
            }
        } else if (tp.getId() > 0) {
            // Вставка з явно вказаним id
            String sql = "INSERT INTO travel_packages (id, name, type, duration_days, base_price, " +
                         "transport, meal_plan, available_seats, rating, description, " +
                         "hotel_stars, guide_name, difficulty_level, insurance_premium) " +
                         "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, tp.getId());
                fillCommonParams(pstmt, tp, 2);
                pstmt.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException("Error inserting package with id=" + tp.getId(), e);
            }
        } else {
            // Вставка без id — автоінкремент SQLite
            String sql = "INSERT INTO travel_packages (name, type, duration_days, base_price, " +
                         "transport, meal_plan, available_seats, rating, description, " +
                         "hotel_stars, guide_name, difficulty_level, insurance_premium) " +
                         "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillCommonParams(pstmt, tp);
                pstmt.executeUpdate();
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        tp.setId(keys.getInt(1));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Error inserting package (auto-id)", e);
            }
        }
    }

    public void updatePackageSeats(int packageId, int newSeats) {
        String sql = "UPDATE travel_packages SET available_seats=? WHERE id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newSeats);
            pstmt.setInt(2, packageId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error updating seats for package id=" + packageId, e);
        }
    }

    public void deletePackage(int id) {
        String sql = "DELETE FROM travel_packages WHERE id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting package id=" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // CRUD: Booking
    // -------------------------------------------------------------------------

    public List<Booking> loadAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT id, package_id, customer_name, contact_info, " +
                     "start_date, end_date, seats_booked, total_price FROM bookings";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Booking(
                        rs.getInt("id"),
                        rs.getInt("package_id"),
                        rs.getString("customer_name"),
                        rs.getString("contact_info"),
                        LocalDate.parse(rs.getString("start_date")),
                        LocalDate.parse(rs.getString("end_date")),
                        rs.getInt("seats_booked"),
                        rs.getDouble("total_price")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading bookings", e);
        }
        return list;
    }

    public List<Booking> listBookings() {
        return Collections.unmodifiableList(loadAllBookings());
    }

    /** Зберігає бронювання і встановлює згенерований БД id у об'єкт Booking. */
    public void saveBooking(Booking booking) {
        String sql = "INSERT INTO bookings (package_id, customer_name, contact_info, " +
                     "start_date, end_date, seats_booked, total_price) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, booking.getPackageId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getContactInfo());
            pstmt.setString(4, booking.getStartDate().toString());
            pstmt.setString(5, booking.getEndDate().toString());
            pstmt.setInt(6, booking.getSeatsBooked());
            pstmt.setDouble(7, booking.getTotalPrice());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving booking", e);
        }
    }

    /** Псевдонім saveBooking() для зворотної сумісності. */
    public void addBooking(Booking booking) {
        saveBooking(booking);
    }

    // -------------------------------------------------------------------------
    // Приватні допоміжні методи
    // -------------------------------------------------------------------------

    /** Заповнює параметри 1..13 для запитів WITHOUT явного id (INSERT auto). */
    private void fillCommonParams(PreparedStatement pstmt, TravelPackage tp) throws SQLException {
        fillCommonParams(pstmt, tp, 1);
    }

    /**
     * Заповнює параметри, починаючи з позиції offset.
     * Порядок: name, type, duration_days, base_price, transport, meal_plan,
     *          available_seats, rating, description,
     *          hotel_stars, guide_name, difficulty_level, insurance_premium
     */
    private void fillCommonParams(PreparedStatement pstmt, TravelPackage tp, int offset) throws SQLException {
        pstmt.setString(offset,     tp.getName());
        pstmt.setString(offset + 1, tp.getType());
        pstmt.setInt   (offset + 2, tp.getDurationDays());
        pstmt.setDouble(offset + 3, tp.getBasePrice());
        pstmt.setString(offset + 4, tp.getTransport().name());
        pstmt.setString(offset + 5, tp.getMealPlan().name());
        pstmt.setInt   (offset + 6, tp.getAvailableSeats());
        pstmt.setDouble(offset + 7, tp.getRating());
        pstmt.setString(offset + 8, tp.getDescription());

        if (tp instanceof RecreationPackage rp) {
            pstmt.setInt   (offset + 9,  rp.getHotelStars());
            pstmt.setNull  (offset + 10, Types.VARCHAR);
            pstmt.setNull  (offset + 11, Types.VARCHAR);
            pstmt.setNull  (offset + 12, Types.DOUBLE);
        } else if (tp instanceof ExcursionPackage ep) {
            pstmt.setNull  (offset + 9,  Types.INTEGER);
            pstmt.setString(offset + 10, ep.getGuideName());
            pstmt.setNull  (offset + 11, Types.VARCHAR);
            pstmt.setNull  (offset + 12, Types.DOUBLE);
        } else if (tp instanceof AdventurePackage ap) {
            pstmt.setNull  (offset + 9,  Types.INTEGER);
            pstmt.setNull  (offset + 10, Types.VARCHAR);
            pstmt.setString(offset + 11, ap.getDifficultyLevel());
            pstmt.setDouble(offset + 12, ap.getInsurancePremium());
        } else {
            pstmt.setNull  (offset + 9,  Types.INTEGER);
            pstmt.setNull  (offset + 10, Types.VARCHAR);
            pstmt.setNull  (offset + 11, Types.VARCHAR);
            pstmt.setNull  (offset + 12, Types.DOUBLE);
        }
    }

    private TravelPackage mapRowToPackage(ResultSet rs) throws SQLException {
        int    id            = rs.getInt("id");
        String name          = rs.getString("name");
        String type          = rs.getString("type");
        int    durationDays  = rs.getInt("duration_days");
        double basePrice     = rs.getDouble("base_price");
        Transport transport  = Transport.valueOf(rs.getString("transport"));
        MealPlan  mealPlan   = MealPlan.valueOf(rs.getString("meal_plan"));
        int    seats         = rs.getInt("available_seats");
        double rating        = rs.getDouble("rating");
        String description   = rs.getString("description");

        String typeLower = type.toLowerCase();
        if (typeLower.equals("sea") || typeLower.equals("cruise")) {
            int hotelStars = rs.getInt("hotel_stars");
            return new RecreationPackage(id, name, type, durationDays, basePrice,
                    transport, mealPlan, seats, rating, description, hotelStars);
        } else if (typeLower.equals("city") || typeLower.equals("excursion")) {
            String guideName = rs.getString("guide_name");
            return new ExcursionPackage(id, name, type, durationDays, basePrice,
                    transport, mealPlan, seats, rating, description, guideName);
        } else if (typeLower.equals("mountain") || typeLower.equals("adventure")) {
            String difficultyLevel  = rs.getString("difficulty_level");
            double insurancePremium = rs.getDouble("insurance_premium");
            return new AdventurePackage(id, name, type, durationDays, basePrice,
                    transport, mealPlan, seats, rating, description, difficultyLevel, insurancePremium);
        } else {
            // Fallback → RecreationPackage
            int hotelStars = rs.getInt("hotel_stars");
            return new RecreationPackage(id, name, type, durationDays, basePrice,
                    transport, mealPlan, seats, rating, description, hotelStars);
        }
    }

    // -------------------------------------------------------------------------
    // Початкове наповнення БД тестовими даними
    // -------------------------------------------------------------------------

    private void seedDatabase() {
        String sql = "INSERT INTO travel_packages (name, type, duration_days, base_price, " +
                     "transport, meal_plan, available_seats, rating, description, " +
                     "hotel_stars, guide_name, difficulty_level, insurance_premium) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 1. Єгипет (Sea → RecreationPackage)
            pstmt.setString(1, "Єгипет, Шарм-ель-Шейх");
            pstmt.setString(2, "Sea");
            pstmt.setInt(3, 7);
            pstmt.setDouble(4, 450.0);
            pstmt.setString(5, Transport.PLANE.name());
            pstmt.setString(6, MealPlan.AI.name());
            pstmt.setInt(7, 25);
            pstmt.setDouble(8, 4.8);
            pstmt.setString(9, "Чудовий відпочинок на Червоному морі, готель 5*");
            pstmt.setInt(10, 5);
            pstmt.setNull(11, Types.VARCHAR);
            pstmt.setNull(12, Types.VARCHAR);
            pstmt.setNull(13, Types.DOUBLE);
            pstmt.addBatch();

            // 2. Альпи (Mountain → AdventurePackage)
            pstmt.setString(1, "Автобусний тур в Альпи");
            pstmt.setString(2, "Mountain");
            pstmt.setInt(3, 5);
            pstmt.setDouble(4, 220.0);
            pstmt.setString(5, Transport.BUS.name());
            pstmt.setString(6, MealPlan.HB.name());
            pstmt.setInt(7, 40);
            pstmt.setDouble(8, 4.5);
            pstmt.setString(9, "Неймовірні краєвиди та свіже гірське повітря Австрійських Альп");
            pstmt.setNull(10, Types.INTEGER);
            pstmt.setNull(11, Types.VARCHAR);
            pstmt.setString(12, "Medium");
            pstmt.setDouble(13, 25.0);
            pstmt.addBatch();

            // 3. Париж (City → ExcursionPackage)
            pstmt.setString(1, "Вікенд у Парижі");
            pstmt.setString(2, "City");
            pstmt.setInt(3, 3);
            pstmt.setDouble(4, 350.0);
            pstmt.setString(5, Transport.PLANE.name());
            pstmt.setString(6, MealPlan.BB.name());
            pstmt.setInt(7, 15);
            pstmt.setDouble(8, 4.7);
            pstmt.setString(9, "Романтична подорож до міста кохання з екскурсією");
            pstmt.setNull(10, Types.INTEGER);
            pstmt.setString(11, "Jean-Pierre");
            pstmt.setNull(12, Types.VARCHAR);
            pstmt.setNull(13, Types.DOUBLE);
            pstmt.addBatch();

            // 4. Круїз (Cruise → RecreationPackage)
            pstmt.setString(1, "Круїз Середземним морем");
            pstmt.setString(2, "Cruise");
            pstmt.setInt(3, 10);
            pstmt.setDouble(4, 1200.0);
            pstmt.setString(5, Transport.SHIP.name());
            pstmt.setString(6, MealPlan.AI.name());
            pstmt.setInt(7, 50);
            pstmt.setDouble(8, 4.9);
            pstmt.setString(9, "Розкішна подорож на великому лайнері містами Італії та Іспанії");
            pstmt.setInt(10, 5);
            pstmt.setNull(11, Types.VARCHAR);
            pstmt.setNull(12, Types.VARCHAR);
            pstmt.setNull(13, Types.DOUBLE);
            pstmt.addBatch();

            pstmt.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException("Error seeding database", e);
        }
    }
}
