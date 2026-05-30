package travelapp;

public class ExcursionPackage extends TravelPackage {
    private String guideName;

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
        // Базова ціна + збір за послуги екскурсовода ($15 за місце)
        return (getBasePrice() + 15.0) * seats;
    }
}
