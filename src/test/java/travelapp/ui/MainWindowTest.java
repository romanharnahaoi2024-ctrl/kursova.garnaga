package travelapp.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import travelapp.service.PackageService;

import java.util.UUID;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class MainWindowTest extends ApplicationTest {

    private ConfigurableApplicationContext springContext;
    private PackageService service;
    private MainWindow mainWindow;

    @Override
    public void start(Stage stage) throws Exception {
        // Create unique in-memory SQLite DB for test isolation
        String uniqueDb = "jdbc:sqlite:file:uitest_" + UUID.randomUUID() + "?mode=memory&cache=shared";
        
        springContext = new SpringApplicationBuilder(travelapp.Application.class)
                .properties(
                        "spring.datasource.url=" + uniqueDb,
                        "spring.jpa.hibernate.ddl-auto=update"
                )
                .run();
        
        service = springContext.getBean(PackageService.class);

        mainWindow = new MainWindow(service);
        Scene scene = new Scene(mainWindow, 1000, 680);
        stage.setScene(scene);
        stage.show();
    }

    @AfterEach
    public void tearDownDb() throws Exception {
        FxToolkit.hideStage();
        if (springContext != null) {
            springContext.close();
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
