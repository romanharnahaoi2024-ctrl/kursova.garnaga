package travelapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import travelapp.service.PackageService;
import travelapp.ui.MainWindow;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

@SpringBootApplication
public class Application extends javafx.application.Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private ConfigurableApplicationContext springContext;
    private PackageService service;

    @Override
    public void init() throws Exception {
        // --- Глобальний обробник непередбачених помилок для не-JavaFX потоків ---
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread [{}]", thread.getName(), throwable);
            Platform.runLater(() -> showCriticalError(throwable));
        });

        logger.info("Initializing Spring Boot application context...");
        springContext = new SpringApplicationBuilder(Application.class).run();
        service = springContext.getBean(PackageService.class);
        logger.info("Spring Boot and Database loaded successfully.");
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
            });

            primaryStage.show();
            logger.info("UI displayed successfully.");
        } catch (Exception e) {
            logger.error("Failed to launch GUI", e);
            showCriticalError(e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping Spring Boot application context...");
        if (springContext != null) {
            springContext.close();
        }
        logger.info("Application successfully stopped.");
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
