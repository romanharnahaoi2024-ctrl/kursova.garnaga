package travelapp.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("recreation")
public class RecreationPackage extends TravelPackage {

    @Column(name = "hotel_stars")
    private int hotelStars;

    // Default constructor for JPA
    protected RecreationPackage() {}

    public RecreationPackage(int id, String name, String type, int durationDays, double basePrice,
                             Transport transport, MealPlan mealPlan, int availableSeats, double rating,
                             int hotelStars) {
        super(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating);
        this.hotelStars = hotelStars;
    }

    public RecreationPackage(int id, String name, String type, int durationDays, double basePrice,
                             Transport transport, MealPlan mealPlan, int availableSeats, double rating,
                             String description, int hotelStars) {
        super(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating, description);
        this.hotelStars = hotelStars;
    }

    public int getHotelStars() {
        return hotelStars;
    }

    public void setHotelStars(int hotelStars) {
        this.hotelStars = hotelStars;
    }

    @Override
    public double calculateFinalPrice(int seats) {
        return (getBasePrice() + hotelStars * 5.0) * seats;
    }
}
