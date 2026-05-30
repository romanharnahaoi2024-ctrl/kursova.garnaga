package travelapp.service;

import travelapp.Booking;
import travelapp.MealPlan;
import travelapp.Transport;
import travelapp.TravelPackage;
import travelapp.storage.PackageRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PackageService {
    private final PackageRepository repo;

    public PackageService(PackageRepository repo) { this.repo = repo; }

    public void load() throws Exception { repo.load(); }
    public void save() throws Exception { repo.save(); }
    public List<TravelPackage> getAll() { return repo.listPackages(); }

    public List<TravelPackage> filter(String type, Transport tr, MealPlan meal, double minPrice, double maxPrice) {
        return repo.listPackages().stream()
                .filter(p -> type == null || type.isBlank() || p.getType().equalsIgnoreCase(type))
                .filter(p -> tr == null || p.getTransport() == tr)
                .filter(p -> meal == null || p.getMealPlan() == meal)
                .filter(p -> minPrice <= 0 || p.getBasePrice() >= minPrice)
                .filter(p -> maxPrice <= 0 || p.getBasePrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<TravelPackage> sortBy(String key) {
        Comparator<TravelPackage> c = switch (key) {
            case "price" -> Comparator.comparingDouble(TravelPackage::getBasePrice);
            case "duration" -> Comparator.comparingInt(TravelPackage::getDurationDays);
            case "rating" -> Comparator.comparingDouble(TravelPackage::getRating);
            default -> Comparator.comparing(TravelPackage::getName);
        };
        return repo.listPackages().stream().sorted(c).collect(Collectors.toList());
    }
    public Booking book(int pkgId, String name, String contact, LocalDate start, LocalDate end, int seats) {
        TravelPackage pkg = repo.findPackageById(pkgId)
                .orElseThrow(() -> new IllegalArgumentException("Пакет не знайдено"));
        if (pkg.getAvailableSeats() < seats)
            throw new IllegalArgumentException("Недостатньо місць");

        // Поліморфний розрахунок вартості залежно від підтипу путівки
        double total = pkg.calculateFinalPrice(seats);
        pkg.setAvailableSeats(pkg.getAvailableSeats() - seats);

        // id=0 → SQLite сам призначить AUTOINCREMENT id і встановить його в об'єкт через RETURN_GENERATED_KEYS
        Booking b = new Booking(0, pkgId, name, contact, start, end, seats, total);
        repo.addBooking(b);
        repo.updatePackage(pkg);
        return b;
    }


    public void saveOrUpdate(TravelPackage pkg) {
        repo.updatePackage(pkg);
    }

    public void delete(int id) {
        repo.deletePackage(id);
    }
}
