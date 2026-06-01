package travelapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "package_id", nullable = false)
    private int packageId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "contact_info", nullable = false)
    private String contactInfo;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    // Default constructor for JPA
    protected Booking() {}

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
