package travelapp;

public class AdventurePackage extends TravelPackage {
    private String difficultyLevel;
    private double insurancePremium;

    public AdventurePackage(int id, String name, String type, int durationDays, double basePrice,
                            Transport transport, MealPlan mealPlan, int availableSeats, double rating,
                            String difficultyLevel, double insurancePremium) {
        super(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating);
        this.difficultyLevel = difficultyLevel;
        this.insurancePremium = insurancePremium;
    }

    public AdventurePackage(int id, String name, String type, int durationDays, double basePrice,
                            Transport transport, MealPlan mealPlan, int availableSeats, double rating,
                            String description, String difficultyLevel, double insurancePremium) {
        super(id, name, type, durationDays, basePrice, transport, mealPlan, availableSeats, rating, description);
        this.difficultyLevel = difficultyLevel;
        this.insurancePremium = insurancePremium;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public double getInsurancePremium() {
        return insurancePremium;
    }

    public void setInsurancePremium(double insurancePremium) {
        this.insurancePremium = insurancePremium;
    }

    @Override
    public double calculateFinalPrice(int seats) {
        // Базова ціна + страхування активного відпочинку за кожне місце
        return (getBasePrice() + insurancePremium) * seats;
    }
}
