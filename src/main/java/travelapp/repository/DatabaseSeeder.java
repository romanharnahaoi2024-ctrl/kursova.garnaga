package travelapp.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import travelapp.model.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final PackageRepository packageRepository;

    public DatabaseSeeder(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @Override
    public void run(String... args) {
        if (packageRepository.count() == 0) {
            logger.info("Seeding database with default travel packages...");

            // 1. Egypt (Sea -> RecreationPackage)
            packageRepository.save(new RecreationPackage(
                    0, "Єгипет, Шарм-ель-Шейх", "Sea", 7, 450.0,
                    Transport.PLANE, MealPlan.AI, 25, 4.8,
                    "Чудовий відпочинок на Червоному морі, готель 5*", 5
            ));

            // 2. Alps (Mountain -> AdventurePackage)
            packageRepository.save(new AdventurePackage(
                    0, "Автобусний тур в Альпи", "Mountain", 5, 220.0,
                    Transport.BUS, MealPlan.HB, 40, 4.5,
                    "Неймовірні краєвиди та свіже гірське повітря Австрійських Альп",
                    "Medium", 25.0
            ));

            // 3. Paris (City -> ExcursionPackage)
            packageRepository.save(new ExcursionPackage(
                    0, "Вікенд у Парижі", "City", 3, 350.0,
                    Transport.PLANE, MealPlan.BB, 15, 4.7,
                    "Романтична подорож до міста кохання з екскурсією", "Jean-Pierre"
            ));

            // 4. Cruise (Cruise -> RecreationPackage)
            packageRepository.save(new RecreationPackage(
                    0, "Круїз Середземним морем", "Cruise", 10, 1200.0,
                    Transport.SHIP, MealPlan.AI, 50, 4.9,
                    "Розкішна подорож на великому лайнері містами Італії та Іспанії", 5
            ));

            logger.info("Database seeding successfully completed.");
        } else {
            logger.info("Database already contains packages. Seeding skipped.");
        }
    }
}
