package travelapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "travel_packages")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@org.hibernate.annotations.DiscriminatorFormula(
    "case when lower(type) in ('sea', 'cruise', 'recreation') then 'recreation' " +
    "when lower(type) in ('city', 'excursion') then 'excursion' " +
    "when lower(type) in ('mountain', 'adventure') then 'adventure' " +
    "else 'recreation' end"
)
public abstract class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(insertable = true, updatable = true)
    private String type;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "base_price", nullable = false)
    private double basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transport transport;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_plan", nullable = false)
    private MealPlan mealPlan;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(nullable = false)
    private double rating;

    private String description;

    // Default constructor for JPA
    protected TravelPackage() {}

    public TravelPackage(int id, String name, String type, int durationDays, double basePrice,
                         Transport transport, MealPlan mealPlan, int availableSeats, double rating) {
        this(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating, "");
    }

    public TravelPackage(int id, String name, String type, int durationDays, double basePrice,
                         Transport transport, MealPlan mealPlan, int availableSeats, double rating, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.durationDays = durationDays;
        this.basePrice = basePrice;
        this.transport = transport;
        this.mealPlan = mealPlan;
        this.availableSeats = availableSeats;
        this.rating = rating;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public Transport getTransport() { return transport; }
    public void setTransport(Transport transport) { this.transport = transport; }

    public MealPlan getMealPlan() { return mealPlan; }
    public void setMealPlan(MealPlan mealPlan) { this.mealPlan = mealPlan; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public abstract double calculateFinalPrice(int seats);

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %d днів | %.2f$ | %s | %s | місць: %d | ★%.1f",
                id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating);
    }
}
