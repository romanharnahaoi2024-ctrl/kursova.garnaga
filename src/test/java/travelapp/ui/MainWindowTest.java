package travelapp.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import travelapp.service.PackageService;
import travelapp.storage.DatabaseManager;
import travelapp.storage.PackageRepository;

import java.util.UUID;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class MainWindowTest extends ApplicationTest {

    private DatabaseManager dbManager;
    private PackageService service;
    private MainWindow mainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        String uniqueDb = "jdbc:sqlite:file:uitest_" + UUID.randomUUID() + "?mode=memory&cache=shared";
        dbManager = new DatabaseManager(uniqueDb);
        PackageRepository repo = new PackageRepository(dbManager);
        repo.load();
        service = new PackageService(repo);

        mainWindow = new MainWindow(service);
        Scene scene = new Scene(mainWindow, 1000, 680);
        stage.setScene(scene);
        stage.show();
    }

    @AfterEach
    public void tearDownDb() throws Exception {
        FxToolkit.hideStage();
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    public void testSwitchTabsAndCheckVisibility() {
        verifyThat("Панель Клієнта", isVisible());
        clickOn("Панель Адміністратора");
        verifyThat("Додати / Редагувати тур", isVisible());
        verifyThat("Зберегти тур", isVisible());
    }

    @Test
    public void testCustomerFilters() {
        clickOn("Панель Клієнта");
        clickOn("Скинути");
        clickOn("Сортувати");
    }
}
