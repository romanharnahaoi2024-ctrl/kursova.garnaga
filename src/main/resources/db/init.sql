-- Таблиця туристичних пакетів
CREATE TABLE IF NOT EXISTS travel_packages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    duration_days INTEGER NOT NULL,
    base_price REAL NOT NULL,
    transport TEXT NOT NULL,
    meal_plan TEXT NOT NULL,
    available_seats INTEGER NOT NULL,
    rating REAL NOT NULL,
    description TEXT,
    hotel_stars INTEGER,
    guide_name TEXT,
    difficulty_level TEXT,
    insurance_premium REAL
);

-- Таблиця бронювань
CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    package_id INTEGER NOT NULL,
    customer_name TEXT NOT NULL,
    contact_info TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    seats_booked INTEGER NOT NULL,
    total_price REAL NOT NULL,
    FOREIGN KEY (package_id) REFERENCES travel_packages(id) ON DELETE CASCADE
);
