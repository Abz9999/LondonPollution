import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

/**
 * This class is responsible for the statistics panel of the database
 * with a styled GUI via CSS.
 * 
 * @author  Gor Vardanyan
 * @version 24.03.2025
 */
public class StatisticsPanel extends Application {
    private StatisticsManager statsManager;
    private ComboBox<String> periodSelector;
    private ComboBox<String> pollutantSelector;
    private ComboBox<String> areaSelector;
    private Button refreshButton;
    private Button backButton;
    private Button mapButton;
    private Tab avgTab;
    private Tab peakTab;
    private Tab trendsTab;
    private Label avgLevelLabel;
    private ListView<String> peakList;
    private VBox trendsPane;
    private Label periodLabel;
    private Label areaLabel;
    private Label avgInfoLabel;
    private Label peakInfoLabel;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        HBox mainLayout = new HBox();
        mainLayout.setId("main-layout"); // ID for CSS styling

        // Control Panel
        VBox controlsPanel = createControlPanel();
        controlsPanel.setId("controls-panel");

        // TabPane
        TabPane statsTabs = new TabPane();
        statsTabs.setPrefSize(800, 600);

        avgTab = new Tab("Average Levels");
        avgTab.setClosable(false);
        VBox avgContent = createAverageStatsPane();
        avgContent.getStyleClass().add("tab-content");
        avgTab.setContent(avgContent);

        peakTab = new Tab("Peak Levels");
        peakTab.setClosable(false);
        VBox peakContent = createPeakStatsPane();
        peakContent.getStyleClass().add("tab-content");
        peakTab.setContent(peakContent);

        trendsTab = new Tab("Trends");
        trendsTab.setClosable(false);
        trendsPane = createTrendsPane();
        trendsPane.getStyleClass().add("tab-content");
        trendsTab.setContent(trendsPane);

        statsTabs.getTabs().addAll(avgTab, peakTab, trendsTab);

        mainLayout.getChildren().addAll(controlsPanel, statsTabs);
        root.setCenter(mainLayout);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add("mystyle.css");
        primaryStage.setTitle("Pollution Statistics Panel");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial load
        loadData("NO2", "2018", "All");

        // Tab switch logic
        statsTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            periodSelector.setDisable(newTab == trendsTab);
            updatePollutantOptions(newTab == trendsTab);
            if (newTab == trendsTab && pollutantSelector.getValue().equals("All")) {
                loadData("All", periodSelector.getValue(), areaSelector.getValue());
            } else {
                if (pollutantSelector.getValue().equals("All")) {
                    pollutantSelector.setValue("NO2");
                }
                loadData(pollutantSelector.getValue(), periodSelector.getValue(), areaSelector.getValue());
            }
        });
    }
    
    /**
     * Handles the back button press to return to the WelcomePanel.
     * Opens a new WelcomePanel window and closes the current StatisticsPanel window.
     * @param event the ActionEvent triggered by the back button
     */
    private void backButtonPressed(ActionEvent event) {
        // Open the WelcomePanel in a new stage
        WelcomePanel welcomePanel = new WelcomePanel();
        Stage welcomePanelStage = new Stage();
        welcomePanel.start(welcomePanelStage);
        welcomePanelStage.show();
        
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }
    
    /**
     * Handles the map button press to proceed to Map Panel.
     */
    private void mapButtonPressed(ActionEvent event) {
        MapPanel mapPanel = new MapPanel();
        Stage mapPanelStage = new Stage();
        mapPanel.start(mapPanelStage);
        mapPanelStage.show();
        
        Stage currentStage = (Stage) mapButton.getScene().getWindow();
        currentStage.close(); 
    }

    /**
     * Updates the pollutantSelector options based on the current tab.
     */
    private void updatePollutantOptions(boolean isTrendsTab) {
        String currentValue = pollutantSelector.getValue();
        pollutantSelector.getItems().clear();
        pollutantSelector.getItems().addAll("NO2", "PM10", "PM2.5");
        if (isTrendsTab) {
            pollutantSelector.getItems().add("All");
        }
        pollutantSelector.setValue(pollutantSelector.getItems().contains(currentValue) ? currentValue : "NO2");
    }

    /**
     * Loads data asynchronously based on selected attributes.
     */
    private void loadData(String pollutant, String year, String area) {
        // Disables the refresh button and show a loading message
        refreshButton.setDisable(true);
        avgLevelLabel.setText("Loading data...");
        peakList.getItems().setAll("Loading data...");
        trendsPane.getChildren().retainAll(trendsPane.getChildren().filtered(node -> node instanceof Label));
        
        // Adds loading label to trends pane and store it for later removal
        Label loadingLabel = new Label("Loading data...");
        trendsPane.getChildren().add(loadingLabel);
    
        Task<StatisticsManager> loadDataTask = new Task<>() {
            @Override
            protected StatisticsManager call() throws Exception {
                DataLoader loader = new DataLoader();
                List<DataSet> dataSets = new ArrayList<>();
                String[] years = {"2018", "2019", "2020", "2021", "2022", "2023"};
                System.out.println("Loading data for pollutant: " + pollutant + ", year: " + year + ", area: " + area);

                if (trendsTab.isSelected() && !pollutant.equals("All")) {
                    for (String y : years) {
                        String fileName = getFileName(pollutant, y);
                        System.out.println("Attempting to load: " + fileName);
                        DataSet dataSet = loader.loadDataFile(fileName, false); // get the UK's data for the given file name
                        if (dataSet != null && !dataSet.getData().isEmpty()) {
                            dataSets.add(dataSet);
                        }
                    }
                } else if (!trendsTab.isSelected()) {
                    String fileName = getFileName(pollutant, year);
                    System.out.println("Attempting to load: " + fileName);
                    DataSet dataSet = loader.loadDataFile(fileName, false); // get the UK's data for the given file name
                    if (dataSet != null && !dataSet.getData().isEmpty()) {
                        dataSets.add(dataSet);
                    }
                }

                if (!dataSets.isEmpty() || pollutant.equals("All")) {
                    StatisticsManager manager = new StatisticsManager(dataSets, area);
                    manager.setSelectedPollutant(pollutant);
                    System.out.println("Created StatisticsManager with " + dataSets.size() + " datasets");
                    return manager;
                }
                System.out.println("No valid datasets, returning null StatisticsManager");
                return null;
            }
        };
    
        // Handles task completion on the JavaFX Application Thread
        loadDataTask.setOnSucceeded(event -> {
            statsManager = loadDataTask.getValue();
            System.out.println("Task succeeded. statsManager is " + (statsManager == null ? "null" : "not null"));
            // Updates UI on JavaFX Application Thread
            javafx.application.Platform.runLater(() -> {
                updateStatistics();
                refreshButton.setDisable(false);
                trendsPane.getChildren().remove(loadingLabel);
                // Ensures Average and Peak labels are updated
                if (statsManager == null) {
                    avgLevelLabel.setText("Average Level: No data");
                    peakList.getItems().setAll("No data available");
                } else {
                    statsManager.updateAverageStats(avgLevelLabel);
                    statsManager.updatePeakStats(peakList);
                };
            });
        });
    
        loadDataTask.setOnFailed(event -> {
            System.err.println("Data loading failed: " + loadDataTask.getException());
            statsManager = null;
            updateStatistics();
            refreshButton.setDisable(false);
            // Removes the loading label from trends pane on failure
            trendsPane.getChildren().remove(loadingLabel);
            // Clears other loading messages
            avgLevelLabel.setText("Average Level: No data");
            peakList.getItems().setAll("No data available");
        });
    
        // Starts the task in a background thread
        new Thread(loadDataTask).start();
    }

    /**
     * Returns the file name for the data source.
     */
    private String getFileName(String pollutant, String year) {
        if (pollutant.equals("NO2")) {
            return "UKAirPollutionData/NO2/mapno2" + year + ".csv";
        } else if (pollutant.equals("PM10")) {
            return "UKAirPollutionData/pm10/mappm10" + year + "g.csv";
        } else if (pollutant.equals("PM2.5") || pollutant.equals("PM25")) {
            return "UKAirPollutionData/pm2.5/mappm25" + year + "g.csv";
        }
        return "";
    }

    /**
     * Updates the displayed statistics.
     */
    private void updateStatistics() {
        System.out.println("Updating statistics. statsManager is " + (statsManager == null ? "null" : "not null"));
        if (statsManager == null) {
            System.out.println("Setting Average and Peak to 'No data'");
            avgLevelLabel.setText("Average Level: No data");
            peakList.getItems().setAll("No data available");
            trendsPane.getChildren().clear();
            trendsPane.getChildren().add(new Label("Pollution Trends"));
            periodLabel.setText("Period: " + periodSelector.getValue());
            areaLabel.setText("Location: " + areaSelector.getValue());
            return;
        }

        System.out.println("Calling updateAverageStats and updatePeakStats");
        statsManager.updateAverageStats(avgLevelLabel);
        statsManager.updatePeakStats(peakList);
        updateTrends();
        
        // Updates the period and area labels
        periodLabel.setText("Period: " + periodSelector.getValue());
        double lat = statsManager.getAverageLatitude();
        double lon = statsManager.getAverageLongitude();
        areaLabel.setText(String.format("Location: %s (Lat: %.6f, Lon: %.6f)",
                areaSelector.getValue(), lat, lon));
    }

    /**
     * Updates the Trends tab with charts.
     */
    private void updateTrends() {
        trendsPane.getChildren().retainAll(trendsPane.getChildren().filtered(node -> node instanceof Label));
        if (statsManager == null) {
            return;
        }
    
        String pollutant = pollutantSelector.getValue();
        String area = areaSelector.getValue();
        String[] years = {"2018", "2019", "2020", "2021", "2022", "2023"};
    
        if (pollutant.equals("All")) {
            String[] pollutants = {"NO2", "PM2.5", "PM10"};
            String[] colors = {"blue", "green", "red"};
            DataLoader loader = new DataLoader();
    
            for (int i = 0; i < pollutants.length; i++) {
                List<DataSet> pollutantData = new ArrayList<>();
                for (String y : years) {
                    String fileName = getFileName(pollutants[i], y);
                    DataSet dataSet = loader.loadDataFile(fileName, false); // get the UK's data for the given file name
                    if (dataSet != null && !dataSet.getData().isEmpty()) {
                        pollutantData.add(dataSet);
                    }
                }
    
                if (!pollutantData.isEmpty()) {
                    StatisticsManager tempManager = new StatisticsManager(pollutantData, area);
                    tempManager.setSelectedPollutant(pollutants[i]);
    
                    NumberAxis xAxis = new NumberAxis(2018, 2023, 1);
                    NumberAxis yAxis = new NumberAxis();
                    xAxis.setLabel("Year");
                    yAxis.setLabel("Pollution Level (" + pollutants[i] + ")");
                    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
                    chart.setTitle(pollutants[i] + " Trend (" + area + ")");
                    chart.setPrefHeight(200);
    
                    tempManager.updateTrendsStats(chart);
                    chart.lookup(".chart-series-line").setStyle("-fx-stroke: " + colors[i] + ";");
    
                    // Add tooltips to data points
                    addDataPointTooltips(chart);
    
                    VBox.setVgrow(chart, Priority.ALWAYS);
                    trendsPane.getChildren().add(chart);
                }
            }
        } else {
            NumberAxis xAxis = new NumberAxis(2018, 2023, 1);
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Year");
            yAxis.setLabel("Pollution Level");
            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle(pollutant + " Trend (" + area + ")");
    
            statsManager.updateTrendsStats(chart);
            chart.lookup(".chart-series-line").setStyle("-fx-stroke: blue;");
    
            // Adds tooltips to data points
            addDataPointTooltips(chart);
    
            VBox.setVgrow(chart, Priority.ALWAYS);
            trendsPane.getChildren().add(chart);
        }
    }
    
    /**
     * Adds tooltips to each data point in the LineChart showing X,Y coordinates
     */
    private void addDataPointTooltips(LineChart<Number, Number> chart) {
        for (LineChart.Series<Number, Number> series : chart.getData()) {
            for (LineChart.Data<Number, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip("X: " + data.getXValue() + "\nY: " + data.getYValue());
                Tooltip.install(data.getNode(), tooltip);
    
                data.getNode().setOnMouseEntered(event -> {
                    tooltip.show(data.getNode(), event.getScreenX() + 10, event.getScreenY() + 10);
                });
                data.getNode().setOnMouseExited(event -> {
                    tooltip.hide();
                });
            }
        }
    }

    /**
     * Creates the Average Levels tab content.
     */
    private VBox createAverageStatsPane() {
        VBox pane = new VBox(10);
        Label title = new Label("Average Pollution Levels");
        title.getStyleClass().add("title-label");
        avgLevelLabel = new Label("Average Level: ");
        periodLabel = new Label("Period: ");
        areaLabel = new Label("Location: ");
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        avgInfoLabel = new Label("Info: This tab represents the average pollution level of the preferred dataset,\n" +
                                        "          as well as the coordinates of a place that matches the same level of pollution");
        avgInfoLabel.setId("info-label");
        HBox infoBox = new HBox(avgInfoLabel);
        infoBox.setId("info-box");
        infoBox.setSpacing(0);
        infoBox.setMinWidth(Region.USE_PREF_SIZE);
        pane.getChildren().addAll(title, avgLevelLabel, periodLabel, areaLabel, spacer, infoBox);
        return pane;
    }

    /**
     * Creates the Peak Levels tab content.
     */
    private VBox createPeakStatsPane() {
        VBox pane = new VBox(10);
        Label title = new Label("Highest Recorded Pollution Levels");
        title.getStyleClass().add("title-label");
        peakList = new ListView<>();
        VBox.setVgrow(peakList, Priority.ALWAYS);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        peakInfoLabel = new Label("Info: This tab represents the highest recorded levels of pollution");
        peakInfoLabel.setId("info-label");
        HBox infoBox = new HBox(peakInfoLabel);
        infoBox.setId("info-box");
        infoBox.setSpacing(0);
        infoBox.setMinWidth(Region.USE_PREF_SIZE);
        pane.getChildren().addAll(title, peakList, spacer, infoBox);
        return pane;
    }

    /**
     * Creates the Trends tab content.
     */
    private VBox createTrendsPane() {
        VBox pane = new VBox(10);
        Label title = new Label("Pollution Trends");
        title.getStyleClass().add("title-label");
        pane.getChildren().addAll(title);
        VBox.setVgrow(pane, Priority.ALWAYS);
        return pane;
    }

    /**
     * Creates the control panel with dropdowns and button.
     */
    private VBox createControlPanel() {
        periodSelector = new ComboBox<>();
        periodSelector.getItems().addAll("2023", "2022", "2021", "2020", "2019", "2018");
        periodSelector.setValue("2023");

        pollutantSelector = new ComboBox<>();
        pollutantSelector.getItems().addAll("NO2", "PM10", "PM2.5");
        pollutantSelector.setValue("NO2");

        areaSelector = new ComboBox<>();
        areaSelector.getItems().addAll("All", "London");
        areaSelector.setValue("All");

        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadData(pollutantSelector.getValue(), periodSelector.getValue(), areaSelector.getValue()));
        
        backButton = new Button("←Back");
        backButton.setOnAction(this::backButtonPressed);
        
        mapButton = new Button("Map");
        mapButton.setOnAction(this::mapButtonPressed);
        
        VBox topControls = new VBox(10, periodSelector, pollutantSelector, areaSelector, refreshButton, backButton);

        // Creates the main panel with a spacer and mapButton
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // Spacer pushes mapButton down
        VBox panel = new VBox(topControls, spacer, mapButton);
        return panel;
    }
}