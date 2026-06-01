package travelapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import travelapp.model.*;
import travelapp.repository.BookingRepository;
import travelapp.repository.PackageRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PackageService {
    private final PackageRepository packageRepository;
    private final BookingRepository bookingRepository;

    public PackageService(PackageRepository packageRepository, BookingRepository bookingRepository) {
        this.packageRepository = packageRepository;
        this.bookingRepository = bookingRepository;
    }

    public void load() throws Exception {
        // No-op: Spring Boot context handles schema creation and database seeding automatically
    }

    public void save() throws Exception {
        packageRepository.flush();
        bookingRepository.flush();
    }

    public List<TravelPackage> getAll() {
        return packageRepository.findAll();
    }

    public List<TravelPackage> filter(String type, Transport tr, MealPlan meal, double minPrice, double maxPrice) {
        return packageRepository.findAll().stream()
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
        return packageRepository.findAll().stream().sorted(c).collect(Collectors.toList());
    }

    public Booking book(int pkgId, String name, String contact, LocalDate start, LocalDate end, int seats) {
        TravelPackage pkg = packageRepository.findById(pkgId)
                .orElseThrow(() -> new IllegalArgumentException("Пакет не знайдено"));
        
        if (pkg.getAvailableSeats() < seats)
            throw new IllegalArgumentException("Недостатньо місць");

        double total = pkg.calculateFinalPrice(seats);
        pkg.setAvailableSeats(pkg.getAvailableSeats() - seats);

        Booking b = new Booking(0, pkgId, name, contact, start, end, seats, total);
        
        // Save using standard JPA repository methods
        Booking savedBooking = bookingRepository.save(b);
        packageRepository.save(pkg);
        
        return savedBooking;
    }

    public void saveOrUpdate(TravelPackage pkg) {
        packageRepository.save(pkg);
    }

    public void delete(int id) {
        packageRepository.deleteById(id);
    }
}
