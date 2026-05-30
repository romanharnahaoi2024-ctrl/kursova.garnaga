package travelapp;

import java.time.LocalDate;

public class Booking {
    private int id;
    private int packageId;
    private String customerName;
    private String contactInfo;
    private LocalDate startDate;
    private LocalDate endDate;
    private int seatsBooked;
    private double totalPrice;

    public Booking(int id, int packageId, String customerName, String contactInfo,
                   LocalDate startDate, LocalDate endDate, int seatsBooked, double totalPrice) {
        this.id = id;
        this.packageId = packageId;
        this.customerName = customerName;
        this.contactInfo = contactInfo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.seatsBooked = seatsBooked;
        this.totalPrice = totalPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPackageId() { return packageId; }
    public String getCustomerName() { return customerName; }
    public String getContactInfo() { return contactInfo; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getSeatsBooked() { return seatsBooked; }
    public double getTotalPrice() { return totalPrice; }

    @Override
    public String toString() {
        return String.format("Booking #%d: %s, seats=%d, total=%.2f$", id, customerName, seatsBooked, totalPrice);
    }
}
