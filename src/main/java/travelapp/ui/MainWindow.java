package travelapp.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import travelapp.*;
import travelapp.service.PackageService;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MainWindow extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private final PackageService service;
    private final TableView<TravelPackage> customerTable = new TableView<>();
    private final TableView<TravelPackage> adminTable = new TableView<>();
    private final ObservableList<TravelPackage> packagesList = FXCollections.observableArrayList();

    // Елементи форми адміністрування
    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField typeField = new TextField();
    private final TextField durationField = new TextField();
    private final TextField priceField = new TextField();
    private final ComboBox<Transport> transportCombo = new ComboBox<>();
    private final ComboBox<MealPlan> mealCombo = new ComboBox<>();
    private final TextField seatsField = new TextField();
    private final TextField ratingField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    public MainWindow(PackageService service) {
        this.service = service;

        // 1. Верхня панель (Header)
        VBox header = new VBox(5);
        header.getStyleClass().add("header-panel");
        Label titleLabel = new Label("TravelApp — Туристичні путівки");
        titleLabel.getStyleClass().add("header-title");
        Label subtitleLabel = new Label("Курсовий проєкт • База даних SQLite та Графічний інтерфейс JavaFX");
        subtitleLabel.getStyleClass().add("header-subtitle");
        header.getChildren().addAll(titleLabel, subtitleLabel);
        setTop(header);

        // 2. Вкладки (TabPane)
        TabPane tabPane = new TabPane();
        Tab customerTab = new Tab("Панель Клієнта", createCustomerPanel());
        customerTab.setClosable(false);

        Tab adminTab = new Tab("Панель Адміністратора", createAdminPanel());
        adminTab.setClosable(false);

        tabPane.getTabs().addAll(customerTab, adminTab);
        setCenter(tabPane);

        // Завантаження початкових даних
        refreshData();
    }

    private void refreshData() {
        try {
            List<TravelPackage> allPackages = service.getAll();
            packagesList.setAll(allPackages);
            customerTable.setItems(packagesList);
            adminTable.setItems(packagesList);
        } catch (Exception e) {
            logger.error("Помилка при оновленні списку турів", e);
            showAlert(Alert.AlertType.ERROR, "Помилка бази даних", "Не вдалося завантажити путівки з БД: " + e.getMessage());
        }
    }

    // --- ПАНЕЛЬ КЛІЄНТА ---
    private Node createCustomerPanel() {
        BorderPane pane = new BorderPane();

        // 1. Ліва панель: Фільтрація
        VBox filterSidebar = new VBox(10);
        filterSidebar.getStyleClass().add("sidebar");
        filterSidebar.setPrefWidth(260);

        Label sidebarTitle = new Label("Фільтрація турів");
        sidebarTitle.getStyleClass().add("form-title");

        Label typeLabel = new Label("Тип туру:");
        typeLabel.getStyleClass().add("form-label");
        TextField filterType = new TextField();
        filterType.setPromptText("напр. Sea, Mountain");

        Label transportLabel = new Label("Транспорт:");
        transportLabel.getStyleClass().add("form-label");
        ComboBox<String> filterTransport = new ComboBox<>();
        filterTransport.getItems().add("Всі");
        for (Transport t : Transport.values()) {
            filterTransport.getItems().add(t.name());
        }
        filterTransport.setValue("Всі");
        filterTransport.setMaxWidth(Double.MAX_VALUE);

        Label mealLabel = new Label("Харчування:");
        mealLabel.getStyleClass().add("form-label");
        ComboBox<String> filterMeal = new ComboBox<>();
        filterMeal.getItems().add("Всі");
        for (MealPlan m : MealPlan.values()) {
            filterMeal.getItems().add(m.name());
        }
        filterMeal.setValue("Всі");
        filterMeal.setMaxWidth(Double.MAX_VALUE);

        Label priceRangeLabel = new Label("Діапазон цін ($):");
        priceRangeLabel.getStyleClass().add("form-label");
        HBox priceBox = new HBox(8);
        TextField minPriceField = new TextField();
        minPriceField.setPromptText("Мін");
        minPriceField.setPrefWidth(90);
        TextField maxPriceField = new TextField();
        maxPriceField.setPromptText("Макс");
        maxPriceField.setPrefWidth(90);
        priceBox.getChildren().addAll(minPriceField, maxPriceField);

        Button applyFiltersBtn = new Button("Застосувати");
        applyFiltersBtn.getStyleClass().add("btn-primary");
        applyFiltersBtn.setMaxWidth(Double.MAX_VALUE);

        Button resetFiltersBtn = new Button("Скинути");
        resetFiltersBtn.getStyleClass().add("btn-secondary");
        resetFiltersBtn.setMaxWidth(Double.MAX_VALUE);

        Label sortLabel = new Label("Сортування:");
        sortLabel.getStyleClass().add("form-label");
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("За назвою", "За ціною", "За рейтингом", "За тривалістю");
        sortCombo.setValue("За назвою");
        sortCombo.setMaxWidth(Double.MAX_VALUE);

        Button applySortBtn = new Button("Сортувати");
        applySortBtn.getStyleClass().add("btn-secondary");
        applySortBtn.setMaxWidth(Double.MAX_VALUE);

        filterSidebar.getChildren().addAll(
                sidebarTitle,
                typeLabel, filterType,
                transportLabel, filterTransport,
                mealLabel, filterMeal,
                priceRangeLabel, priceBox,
                applyFiltersBtn, resetFiltersBtn,
                new Separator(),
                sortLabel, sortCombo, applySortBtn
        );
        pane.setLeft(filterSidebar);

        // 2. Центр: Таблиця турів
        setupTableColumns(customerTable);
        VBox tableContainer = new VBox(15);
        tableContainer.getStyleClass().add("content-area");
        VBox.setVgrow(customerTable, Priority.ALWAYS);

        HBox actionsBar = new HBox();
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        Button bookBtn = new Button("Забронювати вибраний тур");
        bookBtn.getStyleClass().add("btn-success");
        bookBtn.setPrefHeight(40);
        actionsBar.getChildren().add(bookBtn);

        tableContainer.getChildren().addAll(customerTable, actionsBar);
        pane.setCenter(tableContainer);

        // Оброби події
        applyFiltersBtn.setOnAction(e -> {
            String type = filterType.getText();
            String trStr = filterTransport.getValue();
            String mealStr = filterMeal.getValue();

            Transport tr = "Всі".equals(trStr) ? null : Transport.valueOf(trStr);
            MealPlan meal = "Всі".equals(mealStr) ? null : MealPlan.valueOf(mealStr);

            double minPrice = 0;
            double maxPrice = 0;
            try {
                if (!minPriceField.getText().isBlank()) minPrice = Double.parseDouble(minPriceField.getText());
                if (!maxPriceField.getText().isBlank()) maxPrice = Double.parseDouble(maxPriceField.getText());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Помилка вводу", "Ціна повинна бути числовим значенням!");
                return;
            }

            logger.info("Клієнт застосував фільтри: тип={}, транспорт={}, харчування={}, ціна від {} до {}", type, tr, meal, minPrice, maxPrice);
            List<TravelPackage> filtered = service.filter(type, tr, meal, minPrice, maxPrice);
            customerTable.setItems(FXCollections.observableArrayList(filtered));
        });

        resetFiltersBtn.setOnAction(e -> {
            filterType.clear();
            filterTransport.setValue("Всі");
            filterMeal.setValue("Всі");
            minPriceField.clear();
            maxPriceField.clear();
            logger.info("Клієнт скинув фільтри пошуку");
            customerTable.setItems(packagesList);
        });

        applySortBtn.setOnAction(e -> {
            String sortVal = sortCombo.getValue();
            String key = switch (sortVal) {
                case "За ціною"      -> "price";
                case "За рейтингом" -> "rating";
                case "За тривалістю" -> "duration";
                default              -> "name";
            };
            logger.info("Клієнт застосував сортування: {}", sortVal);
            customerTable.setItems(FXCollections.observableArrayList(service.sortBy(key)));
        });

        bookBtn.setOnAction(e -> {
            TravelPackage selected = customerTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Будь ласка, виберіть тур з таблиці для бронювання.");
                return;
            }
            logger.info("Ініційовано бронювання туру ID: {}, Назва: {}", selected.getId(), selected.getName());
            showBookingDialog(selected);
        });

        return pane;
    }

    // --- ПАНЕЛЬ АДМІНІСТРАТОРА ---
    private Node createAdminPanel() {
        BorderPane pane = new BorderPane();

        // 1. Форма редагування (Ліва панель)
        VBox adminForm = new VBox(8);
        adminForm.getStyleClass().add("sidebar");
        adminForm.setPrefWidth(280);

        Label formTitle = new Label("Додати / Редагувати тур");
        formTitle.getStyleClass().add("form-title");

        idField.setEditable(false);
        idField.setPromptText("Автогенерація ID");

        transportCombo.setItems(FXCollections.observableArrayList(Transport.values()));
        transportCombo.setMaxWidth(Double.MAX_VALUE);
        mealCombo.setItems(FXCollections.observableArrayList(MealPlan.values()));
        mealCombo.setMaxWidth(Double.MAX_VALUE);

        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        Button saveBtn = new Button("Зберегти тур");
        saveBtn.getStyleClass().add("btn-success");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        Button clearBtn = new Button("Очистити форму");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setMaxWidth(Double.MAX_VALUE);

        adminForm.getChildren().addAll(
                formTitle,
                new Label("ID туру:"), idField,
                new Label("Назва туру:"), nameField,
                new Label("Тип:"), typeField,
                new Label("Тривалість (днів):"), durationField,
                new Label("Базова ціна ($):"), priceField,
                new Label("Транспорт:"), transportCombo,
                new Label("Харчування:"), mealCombo,
                new Label("Вільні місця:"), seatsField,
                new Label("Рейтинг:"), ratingField,
                new Label("Опис туру:"), descriptionArea,
                saveBtn, clearBtn
        );
        pane.setLeft(adminForm);

        // 2. Таблиця адміністратора (Центр)
        setupTableColumns(adminTable);
        VBox rightPane = new VBox(15);
        rightPane.getStyleClass().add("content-area");
        VBox.setVgrow(adminTable, Priority.ALWAYS);

        HBox controlButtons = new HBox(10);
        Button editBtn = new Button("Редагувати вибраний тур");
        editBtn.getStyleClass().add("btn-primary");

        Button deleteBtn = new Button("Видалити вибраний тур");
        deleteBtn.getStyleClass().add("btn-danger");

        controlButtons.getChildren().addAll(editBtn, deleteBtn);
        rightPane.getChildren().addAll(adminTable, controlButtons);
        pane.setCenter(rightPane);

        // Події форми адміністратора
        editBtn.setOnAction(e -> {
            TravelPackage selected = adminTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Будь ласка, виберіть тур з таблиці для редагування.");
                return;
            }
            idField.setText(String.valueOf(selected.getId()));
            nameField.setText(selected.getName());
            typeField.setText(selected.getType());
            durationField.setText(String.valueOf(selected.getDurationDays()));
            priceField.setText(String.valueOf(selected.getBasePrice()));
            transportCombo.setValue(selected.getTransport());
            mealCombo.setValue(selected.getMealPlan());
            seatsField.setText(String.valueOf(selected.getAvailableSeats()));
            ratingField.setText(String.valueOf(selected.getRating()));
            descriptionArea.setText(selected.getDescription());
            logger.info("Завантажено в форму редагування тур ID: {}", selected.getId());
        });

        deleteBtn.setOnAction(e -> {
            TravelPackage selected = adminTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Будь ласка, виберіть тур з таблиці для видалення.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Підтвердження видалення");
            confirm.setHeaderText("Ви впевнені, що хочете видалити тур?");
            confirm.setContentText(selected.getName() + " (ID: " + selected.getId() + ")");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    logger.warn("Адміністратор видалив тур ID: {}, Назва: {}", selected.getId(), selected.getName());
                    refreshData();
                    clearForm();
                } catch (Exception ex) {
                    logger.error("Помилка при видаленні туру", ex);
                    showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося видалити тур: " + ex.getMessage());
                }
            }
        });

        saveBtn.setOnAction(e -> {
            if (validateAdminForm()) {
                try {
                    TravelPackage pkg = createPackageFromForm();
                    service.saveOrUpdate(pkg);
                    logger.info("Адміністратор зберіг зміни для туру ID: {}, Назва: {}",
                            pkg.getId() == 0 ? "NEW" : pkg.getId(), pkg.getName());
                    showAlert(Alert.AlertType.INFORMATION, "Успіх", "Тур успішно збережено в базі даних!");
                    refreshData();
                    clearForm();
                } catch (Exception ex) {
                    logger.error("Помилка збереження туру", ex);
                    showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося зберегти тур: " + ex.getMessage());
                }
            }
        });

        clearBtn.setOnAction(e -> clearForm());

        return pane;
    }

    /**
     * Фабричний метод: читає поля форми і створює відповідний підклас TravelPackage
     * залежно від вибраного типу туру (Sea/Cruise → RecreationPackage,
     * City/Excursion → ExcursionPackage, Mountain/Adventure → AdventurePackage).
     */
    private TravelPackage createPackageFromForm() {
        int id            = idField.getText().isEmpty() ? 0 : Integer.parseInt(idField.getText());
        String name       = nameField.getText().trim();
        String type       = typeField.getText().trim();
        int duration      = Integer.parseInt(durationField.getText().trim());
        double price      = Double.parseDouble(priceField.getText().trim());
        Transport transport = transportCombo.getValue();
        MealPlan mealPlan   = mealCombo.getValue();
        int seats         = Integer.parseInt(seatsField.getText().trim());
        double rating     = Double.parseDouble(ratingField.getText().trim());
        String description = descriptionArea.getText().trim();

        String typeLower = type.toLowerCase();
        if (typeLower.equals("sea") || typeLower.equals("cruise")) {
            return new RecreationPackage(id, name, type, duration, price, transport, mealPlan, seats, rating, description, 3);
        } else if (typeLower.equals("city") || typeLower.equals("excursion")) {
            return new ExcursionPackage(id, name, type, duration, price, transport, mealPlan, seats, rating, description, "");
        } else if (typeLower.equals("mountain") || typeLower.equals("adventure")) {
            return new AdventurePackage(id, name, type, duration, price, transport, mealPlan, seats, rating, description, "Medium", 20.0);
        } else {
            // Fallback — RecreationPackage
            return new RecreationPackage(id, name, type, duration, price, transport, mealPlan, seats, rating, description, 0);
        }
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        typeField.clear();
        durationField.clear();
        priceField.clear();
        transportCombo.setValue(null);
        mealCombo.setValue(null);
        seatsField.clear();
        ratingField.clear();
        descriptionArea.clear();
    }

    private boolean validateAdminForm() {
        if (nameField.getText().isBlank() || typeField.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Валідація", "Назва та тип туру не можуть бути порожніми.");
            return false;
        }
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            int seats = Integer.parseInt(seatsField.getText().trim());
            double rating = Double.parseDouble(ratingField.getText().trim());

            if (duration <= 0 || price <= 0 || seats < 0 || rating < 0 || rating > 5) {
                showAlert(Alert.AlertType.WARNING, "Валідація", "Будь ласка, введіть коректні додатні значення. Рейтинг має бути від 0 до 5.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Валідація", "Тривалість, ціна, місця та рейтинг повинні бути числовими значеннями.");
            return false;
        }

        if (transportCombo.getValue() == null || mealCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Валідація", "Будь ласка, виберіть тип транспорту та харчування.");
            return false;
        }

        return true;
    }

    // --- МОДАЛЬНЕ ВІКНО БРОНЮВАННЯ ---
    private void showBookingDialog(TravelPackage selectedPkg) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Бронювання туру");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #ffffff;");

        Label title = new Label("Оформлення бронювання");
        title.getStyleClass().add("form-title");

        Label tourLabel = new Label("Вибраний тур: " + selectedPkg.getName() + " (" + selectedPkg.getBasePrice() + "$ / день)");
        tourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4f46e5;");

        TextField custNameField = new TextField();
        custNameField.setPromptText("Ім'я клієнта");

        TextField custContactField = new TextField();
        custContactField.setPromptText("Контактний номер / Email");

        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusDays(selectedPkg.getDurationDays()));

        TextField seatsBookedField = new TextField("1");
        seatsBookedField.setPromptText("Кількість місць");

        Label totalLabel = new Label("Загальна вартість: " + selectedPkg.getBasePrice() + "$");
        totalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #10b981; -fx-padding: 10px 0;");

        // Динамічний перерахунок вартості
        seatsBookedField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.trim().isEmpty()) {
                    int seats = Integer.parseInt(newValue.trim());
                    double total = selectedPkg.getBasePrice() * seats;
                    totalLabel.setText("Загальна вартість: " + String.format("%.2f", total) + "$");
                }
            } catch (NumberFormatException e) {
                totalLabel.setText("Загальна вартість: —");
            }
        });

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button confirmBtn = new Button("Підтвердити");
        confirmBtn.getStyleClass().add("btn-success");
        Button cancelBtn = new Button("Скасувати");
        cancelBtn.getStyleClass().add("btn-secondary");
        btnBox.getChildren().addAll(cancelBtn, confirmBtn);

        layout.getChildren().addAll(
                title,
                tourLabel,
                new Label("Ваше ім'я:"), custNameField,
                new Label("Контакти:"), custContactField,
                new Label("Дата початку:"), startPicker,
                new Label("Дата закінчення:"), endPicker,
                new Label("Кількість місць (доступно " + selectedPkg.getAvailableSeats() + "):"), seatsBookedField,
                totalLabel,
                btnBox
        );

        Scene scene = new Scene(layout, 350, 520);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);

        cancelBtn.setOnAction(e -> dialog.close());

        confirmBtn.setOnAction(e -> {
            String name = custNameField.getText().trim();
            String contact = custContactField.getText().trim();
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            String seatsStr = seatsBookedField.getText().trim();

            if (name.isEmpty() || contact.isEmpty() || start == null || end == null || seatsStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Валідація", "Будь ласка, заповніть всі поля.");
                return;
            }

            if (end.isBefore(start)) {
                showAlert(Alert.AlertType.WARNING, "Валідація", "Дата закінчення не може бути раніше дати початку.");
                return;
            }

            try {
                int seats = Integer.parseInt(seatsStr);
                if (seats <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Валідація", "Кількість місць повинна бути більшою за 0.");
                    return;
                }
                if (seats > selectedPkg.getAvailableSeats()) {
                    showAlert(Alert.AlertType.WARNING, "Валідація", "Немає стільки вільних місць! Доступно: " + selectedPkg.getAvailableSeats());
                    return;
                }

                // Виклик логіки бронювання
                Booking b = service.book(selectedPkg.getId(), name, contact, start, end, seats);
                logger.info("Клієнт '{}' успішно забронював тур ID={}. Номер бронювання: {}, Кількість місць: {}, Загальна ціна: {}$",
                        name, selectedPkg.getId(), b.getId(), seats, b.getTotalPrice());

                showAlert(Alert.AlertType.INFORMATION, "Бронювання успішне",
                        String.format("Тур заброньовано!\nНомер квитка: #%d\nКлієнт: %s\nЗагальна сума: %.2f$",
                                b.getId(), b.getCustomerName(), b.getTotalPrice()));

                dialog.close();
                refreshData();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Валідація", "Введіть числове значення для кількості місць.");
            } catch (Exception ex) {
                logger.error("Помилка під час створення бронювання", ex);
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося виконати бронювання: " + ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    // --- ДОПОМІЖНІ МЕТОДИ ---
    @SuppressWarnings("unchecked")
    private void setupTableColumns(TableView<TravelPackage> table) {
        TableColumn<TravelPackage, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<TravelPackage, String> colName = new TableColumn<>("Назва");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(160);

        TableColumn<TravelPackage, String> colType = new TableColumn<>("Тип");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        colType.setPrefWidth(70);

        TableColumn<TravelPackage, Integer> colDuration = new TableColumn<>("Днів");
        colDuration.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDurationDays()).asObject());
        colDuration.setPrefWidth(55);

        TableColumn<TravelPackage, Double> colPrice = new TableColumn<>("Ціна");
        colPrice.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getBasePrice()).asObject());
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f$", price));
                }
            }
        });
        colPrice.setPrefWidth(65);

        TableColumn<TravelPackage, String> colTransport = new TableColumn<>("Транспорт");
        colTransport.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransport().name()));
        colTransport.setPrefWidth(85);

        TableColumn<TravelPackage, String> colMeal = new TableColumn<>("Харчування");
        colMeal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMealPlan().name()));
        colMeal.setPrefWidth(90);

        TableColumn<TravelPackage, Integer> colSeats = new TableColumn<>("Місця");
        colSeats.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAvailableSeats()).asObject());
        colSeats.setPrefWidth(60);

        TableColumn<TravelPackage, Double> colRating = new TableColumn<>("Рейтинг");
        colRating.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getRating()).asObject());
        colRating.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    setText(String.format("★%.1f", rating));
                }
            }
        });
        colRating.setPrefWidth(65);

        TableColumn<TravelPackage, String> colDesc = new TableColumn<>("Опис");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colDesc.setPrefWidth(180);

        table.getColumns().clear();
        table.getColumns().addAll(colId, colName, colType, colDuration, colPrice, colTransport, colMeal, colSeats, colRating, colDesc);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void showAlert(Alert.AlertType type, String title, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
