import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a panel for retrieving pollution data for specific grid coordinates.
 *
 * @author Frankie Cole
 * @version 1.0
 */
public class GridDataPanel extends Application
{
    // UI components for user input and interaction
    private ChoiceBox<String> pollutantChoices;
    private ChoiceBox<String> yearChoices;
    private TextField xCoordinateField;
    private TextField yCoordinateField;
    private Button searchGridData;
    private Button backButton;
    private Label resultLabel;
    private StatisticsManager statsManager;
    
    // Static instance of MapPanel to determine grid dimensions
    private static final MapPanel MAP_PANEL = new MapPanel();

    /**
     * The main entry point for JavaFX programs.
     * 
     * @param stage The primary stage for this application.
     */
    @Override
    public void start(Stage stage)
    {
        // Initialize UI components
        pollutantChoices = new ChoiceBox<>();
        yearChoices = new ChoiceBox<>();
        xCoordinateField = new TextField();
        yCoordinateField = new TextField();
        resultLabel = new Label("Pollution Level: ");
        
        VBox inputField = new VBox();
        BorderPane root = new BorderPane();
        
        // Configure and arrange UI elements
        createWindow(inputField, root);
        
        // Attach event handler to search button
        searchGridData.setOnAction(this::searchButtonPressed);
        
        // Create and set up the scene
        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Grid Data Panel");
        scene.getStylesheets().add(getClass().getResource("mystyle.css").toExternalForm());

        // Display the stage
        stage.setScene(scene); 
        stage.show();
    }
    
    /**
     * Creates the main window layout and UI components.
     * 
     * @param inputField VBox container for input fields.
     * @param root BorderPane container for organizing the layout.
     */
    private void createWindow(VBox inputField, BorderPane root) {
        // Title label for the panel
        Label title = new Label("Grid Data Panel");
        title.getStyleClass().add("title-label");
        
        // Select pollutant choicebox
        Label pollutantChoicesLabel = new Label("Select a pollutant");
        pollutantChoices.getItems().addAll("no2", "pm25", "pm10");
        
        // Select year choicebox
        Label yearChoicesLabel = new Label("Select a year");
        yearChoices.getItems().addAll("2018", "2019", "2020", "2021", "2022", "2023");
        
        // Select x-Coordinate
        Label xCoordinateLabel = new Label("Enter the x-Coordinate of the grid");
        xCoordinateField.setPromptText("x-Coordinate (0 to "+ (MAP_PANEL.getColumns() - 1) +")");
        xCoordinateField.setMaxWidth(200);
        
        // Select y-Coordinate
        Label yCoordinateLabel = new Label("Enter the y-Coordinate of the grid");
        yCoordinateField.setPromptText("y-Coordinate (0 to " + (MAP_PANEL.getRows() - 1) +")");
        yCoordinateField.setMaxWidth(200);
        
        // Search button
        searchGridData = new Button("Search grid data");
        
        // Apply styling and layout properties
        inputField.getStyleClass().add("controls-panel");
        inputField.setPadding(new Insets(15));
        inputField.setSpacing(15);
        
        // Add all UI components to the input field container
        inputField.getChildren().addAll(
            title,
            pollutantChoicesLabel, pollutantChoices,
            yearChoicesLabel, yearChoices,
            xCoordinateLabel, xCoordinateField,
            yCoordinateLabel, yCoordinateField,
            searchGridData,
            resultLabel  // Add the result label to display the pollution level
        );
        
        // Logic for back button
        backButton = new Button("←Back");
        backButton.setOnAction(this::backButtonPressed);
        inputField.getChildren().add(backButton);
        
        // Layout for displaying results
        VBox resultPane = new VBox(15);
        resultPane.getStyleClass().add("result-pane");
        resultPane.setPadding(new Insets(15));
        Label resultTitle = new Label("Results");
        resultTitle.getStyleClass().add("title-label");
        resultPane.getChildren().addAll(resultTitle, resultLabel);
        
        // An HBox to place inputField and resultPane side by side
        HBox mainContent = new HBox(30);
        mainContent.setAlignment(javafx.geometry.Pos.CENTER);
        mainContent.getChildren().addAll(inputField, resultPane);
        
        // Allows the HBox to grow and center its children
        HBox.setHgrow(inputField, Priority.SOMETIMES);
        HBox.setHgrow(resultPane, Priority.SOMETIMES);
        
        // Adds the HBox to the center of the BorderPane
        root.getStyleClass().add("root");
        root.setCenter(mainContent);
        BorderPane.setMargin(mainContent, new Insets(20));
    }
    
    /**
     * Handles the search button click event.
     * 
     * @param event The event triggered by clicking the search button
     */
    private void searchButtonPressed(ActionEvent event) {
        String pollutantOption = pollutantChoices.getValue();
        if(pollutantOption == null) {
            resultLabel.setText("Pollution Level: Choose a pollutant");
            return;
        }
        
        // Checks a year was selected
        String yearOption = yearChoices.getValue();
        if(yearOption == null) {
            resultLabel.setText("Pollution Level: Choose a year");
            return;
        }
        
        String xCoordinateInput = xCoordinateField.getText().trim();
        Integer xCoordinateValue = null;
        // Checks user inputed a valid x coordinate
        if (xCoordinateInput.isEmpty()) {
                System.out.println("Please enter a x coordinate.");
                resultLabel.setText("Pollution Level: Please enter an x coordinate");
                return;
        } else {
                try {
                    xCoordinateValue = Integer.parseInt(xCoordinateInput); // Convert to int
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a valid x coordinate.");
                    resultLabel.setText("Pollution Level: Invalid x coordinate");
                    return;
                }
        }
        
        String yCoordinateInput = yCoordinateField.getText().trim();
        Integer yCoordinateValue = null;
        // checks user inputed a valid y coordinate
        if (yCoordinateInput.isEmpty()) {
                System.out.println("Please enter a y coordinate.");
                resultLabel.setText("Pollution Level: Please enter a y coordinate");
                return;
        } else {
                try {
                    yCoordinateValue = Integer.parseInt(yCoordinateInput); // Convert to int
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a valid y coordinate.");
                    resultLabel.setText("Pollution Level: Invalid y coordinate");
                    return;
                }
        }
        
        // Process data with validated inputs
        // Convert pollutant to uppercase to match StatisticsManager
        processData(pollutantOption.toUpperCase(), yearOption, xCoordinateValue, yCoordinateValue);
    }
    
    /**
     * Loads pollution data and processes it for the given coordinates.
     */
    private void processData(String pollutantOption, String yearOption, int xCoordinateValue, int yCoordinateValue) {
        DataLoader loader = new DataLoader();
        List<DataSet> dataSets = new ArrayList<>();
        String fileName = getFileName(pollutantOption, yearOption);
        DataSet dataSet = loader.loadDataFile(fileName, true); // Load London's data for the given file name
        
        if (dataSet != null && !dataSet.getData().isEmpty()) {
            dataSets.add(dataSet);
            System.out.println("Loaded dataset with " + dataSet.getData().size() + " data points.");
        } else {
            System.out.println("No data loaded for file: " + fileName);
        }

        // Create StatisticsManager with the loaded data (default area to "All" for simplicity)
        statsManager = new StatisticsManager(dataSets, "All");
        statsManager.setSelectedPollutant(pollutantOption);

        // Debug: Print some sample data points to verify the coordinates in the dataset
        if (dataSet != null) {
            List<DataPoint> dataPoints = dataSet.getData();
            System.out.println("Sample data points (first 5):");
            for (int i = 0; i < Math.min(5, dataPoints.size()); i++) {
                DataPoint dp = dataPoints.get(i);
                System.out.println("DataPoint " + i + ": Easting=" + dp.x() + ", Northing=" + dp.y() + ", Value=" + dp.value());
            }
        }

        // Convert coordinates and get the pollution level
        convertCoordinatesToEastAndNorthings(xCoordinateValue, yCoordinateValue);
    }
    
    /**
     * Converts grid coordinates to easting and northing values.
     */
    private void convertCoordinatesToEastAndNorthings(int xCoordinate, int yCoordinate) {
        GridData gridData = new GridData();
        // Define the bounds and grid dimensions as per MapPanel
        int maxLeftEasting = gridData.getMaxLeft();
        int maxRightEasting = gridData.getMaxRight();
        int maxTopNorthing = gridData.getMaxTop();
        int maxBottomNorthing = gridData.getMaxBottom();
        
        int rows = MAP_PANEL.getRows();     // MapPanel's ROWS
        int columns = MAP_PANEL.getColumns();  // MapPanel's COLUMNS
        
        // Validates that xCoordinate and yCoordinate are within grid bounds
        if (xCoordinate < 0 || xCoordinate >= columns) {
            System.out.println("x-coordinate out of grid bounds (0 to " + (columns - 1) + ").");
            resultLabel.setText("Pollution Level: x-coordinate out of bounds (0 to " + (columns - 1) + ")");
            return;
        }
        if (yCoordinate < 0 || yCoordinate >= rows) {
            System.out.println("y-coordinate out of grid bounds (0 to " + (rows - 1) + ").");
            resultLabel.setText("Pollution Level: y-coordinate out of bounds (0 to " + (rows - 1) + ")");
            return;
        }

        // Converts grid indices (xCoordinate, yCoordinate) to Easting and Northing
        int eastingCoordinate = xCoordinate * 1000 + maxLeftEasting;        
        int northingCoordinate = maxTopNorthing - yCoordinate * 1000;
        
        // Checks if the coordinates are within the map bounds (should always be true due to prior validation)
        if (!gridData.isWithinRegion(eastingCoordinate, northingCoordinate)) {
            System.out.println("Enter coordinates within the map bounds.");
            resultLabel.setText("Pollution Level: Coordinates out of bounds");
            return;
        }
        
        // Gets the pollution level using StatisticsManager
        if (statsManager != null) {
            System.out.println("Searching for pollution level at Easting=" + eastingCoordinate + ", Northing=" + northingCoordinate);
            double pollutionLevel = statsManager.getPollutionLevelForCoordinates(eastingCoordinate, northingCoordinate);
            if (pollutionLevel != -1) {
                resultLabel.setText(String.format("Pollution Level: %.2f µg/m³", pollutionLevel));
            } else {
                resultLabel.setText("Pollution Level: No data found for coordinates");
            }
        } else {
            resultLabel.setText("Pollution Level: No data loaded");
        }
        
        System.out.println("Easting : " + eastingCoordinate);
        System.out.println("Northing : " + northingCoordinate);
    }
    
    /**
     * Handles the back button click event.
     * Opens the WelcomePanel and closes the current window.
     */
    private void backButtonPressed(ActionEvent event) {
        WelcomePanel welcomePanel = new WelcomePanel();
        Stage welcomePanelStage = new Stage();
        welcomePanel.start(welcomePanelStage);
        welcomePanelStage.show();
        
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }
    
    /**
     * Constructs the file name for loading pollution data based on pollutant and year.
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
}