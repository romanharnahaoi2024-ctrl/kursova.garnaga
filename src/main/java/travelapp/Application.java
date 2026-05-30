package travelapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import travelapp.service.PackageService;
import travelapp.storage.PackageRepository;
import travelapp.ui.MainWindow;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private PackageService service;

    @Override
    public void init() throws Exception {
        // --- Глобальний обробник непередбачених помилок для не-JavaFX потоків ---
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread [{}]", thread.getName(), throwable);
            Platform.runLater(() -> showCriticalError(throwable));
        });

        logger.info("Initializing database connection...");
        PackageRepository repo = new PackageRepository("data/packages.csv", "data/bookings.csv");
        service = new PackageService(repo);
        service.load();
        logger.info("Database loaded and initialized successfully.");
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting JavaFX Main UI...");
        try {
            MainWindow mainWindow = new MainWindow(service);
            Scene scene = new Scene(mainWindow, 1000, 680);

            // Підключення CSS стилів
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            // --- Глобальний обробник непередбачених помилок для JavaFX потоку ---
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
                logger.error("Uncaught exception in JavaFX thread", throwable);
                showCriticalError(throwable);
            });

            primaryStage.setScene(scene);
            primaryStage.setTitle("TravelApp — Система управління туристичними путівками");

            // Коректне завершення роботи
            primaryStage.setOnCloseRequest(event -> {
                logger.info("User requested application shutdown.");
                try {
                    service.save();
                } catch (Exception e) {
                    logger.error("Failed to save data on close", e);
                }
                logger.info("Application successfully stopped.");
            });

            primaryStage.show();
            logger.info("UI displayed successfully.");
        } catch (Exception e) {
            logger.error("Failed to launch GUI", e);
            showCriticalError(e);
        }
    }

    /** Показує діалог з повідомленням про критичну помилку. */
    private static void showCriticalError(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Критична помилка");
        alert.setHeaderText("Сталася непередбачена помилка");
        alert.setContentText(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        logger.info("Bootstrap main method called");
        launch(args);
    }
}
