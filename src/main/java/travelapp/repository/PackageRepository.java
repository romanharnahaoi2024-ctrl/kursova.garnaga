package travelapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import travelapp.model.TravelPackage;

@Repository
public interface PackageRepository extends JpaRepository<TravelPackage, Integer> {
}
