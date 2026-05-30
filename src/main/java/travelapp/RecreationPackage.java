package travelapp;

public class RecreationPackage extends TravelPackage {
    private int hotelStars;

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
        // Базова ціна + курортний збір залежно від зірковості готелю ($5 за зірку) за кожне місце
        return (getBasePrice() + hotelStars * 5.0) * seats;
    }
}
