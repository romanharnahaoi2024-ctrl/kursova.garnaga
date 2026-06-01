package travelapp.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("excursion")
public class ExcursionPackage extends TravelPackage {

    @Column(name = "guide_name")
    private String guideName;

    // Default constructor for JPA
    protected ExcursionPackage() {}

    public ExcursionPackage(int id, String name, String type, int durationDays, double basePrice,
                            Transport transport, MealPlan mealPlan, int availableSeats, double rating,
                            String guideName) {
        super(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating);
        this.guideName = guideName;
    }

    public ExcursionPackage(int id, String name, String type, int durationDays, double basePrice,
                            Transport transport, MealPlan mealPlan, int availableSeats, double rating,
                            String description, String guideName) {
        super(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating, description);
        this.guideName = guideName;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    @Override
    public double calculateFinalPrice(int seats) {
        return (getBasePrice() + 15.0) * seats;
    }
}
