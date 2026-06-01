package travelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import travelapp.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
}
