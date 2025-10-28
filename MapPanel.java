import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.event.*;
import javafx.scene.paint.Color;
import java.io.File;
import javafx.scene.canvas.*;

/**
 * MapPanel displays a grid map with pollutant data and allows the user to interact with it by selecting
 * different pollutants, years and clicking on grid cells to view detailed information.
 * 
 * To start the application, use BlueJ's 'Run JavaFX Application' function.
 *
 * @author Patrick Dunham
 * @version 24.03.2025
 */
public class MapPanel extends Application
{
    // Fields:
    private MapManager mapManager;
    
    private Stage stage;
    private Label imageLabel;
    private Label statusLabel;
    
    private Button backButton;
    private Button statsButton;
    private Button gridDataButton;
    private Button clearButton;
    
    private ComboBox<String> pollutantComboBox;
    private ComboBox<String> yearComboBox;
    
    private Label gridCodeLabel;
    private Label xLabel;
    private Label yLabel;
    private Label valueLabel;
    
    // Static fields:
    private static final String VERSION = "Version 1.0";
    
    private static final String GRID_CODE_LABEL_PREFIX = "UK grid code: ";
    private static final String X_LABEL_PREFIX  = "X: ";
    private static final String Y_LABEL_PREFIX = "Y: ";
    private static final String VALUE_LABEL_PREFIX = "Value: ";
    private static final String NOT_AVAILABLE = "N/A";
    
    private static final float LOW_OPACITY = 0.5f;
    private static final float HIGH_OPACITY = 1.0f;
    
    private static final int GRID_SIZE = 24;
    private static final int COLUMNS = 43;
    private static final int ROWS = 24;
    
    private static final int IMAGE_WIDTH = COLUMNS * GRID_SIZE;
    private static final int IMAGE_HEIGHT = ROWS * GRID_SIZE;
    
    private Canvas gridCanvas;
    
    /**
     * Initialise the MapPanel.
     */
    public MapPanel()
    {
        mapManager = new MapManager(this); // Initialize map manager to handle map logic
        mapManager.resetVariables(); // Reset map variables
    }

    // ---- JavaFX entry point and initial window construction ----

    /**
     * The main entry point for JavaFX programs.
     * 
     * @param stage The main stage for the application
     */
    @Override
    public void start(Stage stage)
    {
        this.stage = stage;

        // Set up the root container and layout components
        VBox root = new VBox();
        FlowPane topButtons = new FlowPane(); // Buttons at the top of the window
        topButtons.setHgap(5); // Set horizontal gap between buttons
        
        // Initialize and set actions for buttons
        backButton = new Button("←Back");
        backButton.setOnAction(this::backButtonPressed);
        
        statsButton = new Button("Statistics");
        statsButton.setOnAction(this::statsButtonPressed);
        
        gridDataButton = new Button("Grid Data");
        gridDataButton.setOnAction(this::gridDataButtonButtonPressed);
        
        // Add buttons to the topButtons pane
        topButtons.getChildren().addAll(backButton, statsButton, gridDataButton);
        
        // Set up the map image view
        ImageView londonView = new ImageView(ImageFileManager.loadImage("London.png"));
        londonView.setFitWidth(IMAGE_WIDTH);
        londonView.setFitHeight(IMAGE_HEIGHT);
        imageLabel = new Label("", londonView); // Label for displaying the map image
        imageLabel.setId("image");
        
        gridCanvas = new Canvas(IMAGE_WIDTH, IMAGE_HEIGHT); // Create a canvas for drawing grid cells
        gridCanvas.setOnMouseClicked(this::cellClicked);
        
        // Stack the map image and grid canvas on top of each other
        StackPane stackPane = new StackPane(londonView, gridCanvas);
        statusLabel = new Label(VERSION); // Label for displaying the version information
        statusLabel.getStyleClass().add("infoLabel"); // Apply style class
        
        // ComboBoxes for selecting pollutants and years
        pollutantComboBox = new ComboBox<>();
        pollutantComboBox.getItems().addAll("no2", "pm25", "pm10");
        pollutantComboBox.setPromptText("Select pollutant");
        pollutantComboBox.setOnAction(this::selectedPollutant);
        
        yearComboBox = new ComboBox<>();
        yearComboBox.getItems().addAll("2018", "2019", "2020", "2021", "2022", "2023");
        yearComboBox.setPromptText("Select year");
        yearComboBox.setOnAction(this::selectedYear);
        
        // Clear button for resetting selectionsr
        clearButton = new Button("Clear");
        clearButton.setOnAction(this::clearSelection);
        
        // Labels to display grid information
        gridCodeLabel = new Label(GRID_CODE_LABEL_PREFIX + NOT_AVAILABLE);
        xLabel = new Label(X_LABEL_PREFIX + NOT_AVAILABLE);
        yLabel = new Label(Y_LABEL_PREFIX + NOT_AVAILABLE);
        valueLabel = new Label(VALUE_LABEL_PREFIX + NOT_AVAILABLE);
        
        // Toolbar layout
        Pane toolBar = new VBox(pollutantComboBox, yearComboBox, clearButton, gridCodeLabel, xLabel, yLabel, valueLabel);
        toolBar.setId("toolbar");
        
        // Content layout for the entire window
        Pane contentPane = new BorderPane(stackPane, topButtons, null, statusLabel, toolBar);
        contentPane.setId("content");

        root.getChildren().add(contentPane); // Add content to the root layout

        // Set up the scene and apply styles
        Scene scene = new Scene(root);
        scene.getStylesheets().add("mystyle.css");

        stage.setTitle("Map Panel");
        stage.setScene(scene);
        stage.show();
    }
    
    // ---- support methods ----
    
    /**
     * Returns the number of columns in the grid.
     * 
     * @return The number of columns
     */
    public int getColumns() {
        return COLUMNS;
    }
    
    /**
     * Returns the number of rows in the grid.
     * 
     * @return The number of rows
     */
    public int getRows() {
        return ROWS;
    }
    
    /**
     * Handles the back button logic.
     * 
     * @param event The action event triggered by clicking the back button
     */
    private void backButtonPressed(ActionEvent event) {
        WelcomePanel welcomePanel = new WelcomePanel();
        Stage welcomeStage = new Stage();
        welcomePanel.start(welcomeStage);
        welcomeStage.show();
        stage.close(); // close this window
    }
    
    /**
     * Handles the statistics button logic.
     * 
     * @param event The action event triggered by clicking the statistics button
     */
    private void statsButtonPressed(ActionEvent event) {
        StatisticsPanel statsPanel = new StatisticsPanel();
        Stage statsStage = new Stage();
        statsPanel.start(statsStage);
        statsStage.show();
        stage.close(); // close this window
    }
    
    /**
     * Handles the grid data button logic.
     * 
     * @param event The action event triggered by clicking the grid data button
     */
    private void gridDataButtonButtonPressed(ActionEvent event) {
        GridDataPanel gridDataPanel = new GridDataPanel();
        Stage gridDataStage = new Stage();
        gridDataPanel.start(gridDataStage);
        gridDataStage.show();
        stage.close(); // close this window
    }
    
    /**
     * Clears all selections and resets the map state.
     * 
     * @param event The action event triggered by clicking the clear button
     */
    private void clearSelection(ActionEvent event) {
        mapManager.resetVariables(); // Reset map variables
        clearGrid(); // Clear grid and canvas
        pollutantComboBox.getSelectionModel().clearSelection(); // Clear pollutant selection
        yearComboBox.getSelectionModel().clearSelection(); // Clear year selection
    }
    
    /**
     * Clears all the grid data and canvas.
     * Resets the labels.
     */
    public void clearGrid() {
        mapManager.resetGrid(); // Reset the grid data
        
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight()); // Clear the canvas
        
        // Reset grid labels to "N/A" (i.e. Not Available)
        gridCodeLabel.setText(GRID_CODE_LABEL_PREFIX + NOT_AVAILABLE);
        xLabel.setText(X_LABEL_PREFIX + NOT_AVAILABLE);
        yLabel.setText(Y_LABEL_PREFIX + NOT_AVAILABLE);
        valueLabel.setText(VALUE_LABEL_PREFIX + NOT_AVAILABLE);
    }
    
    /**
     * Sets the color and draws a cell on the canvas for a specific grid location.
     * 
     * @param col The column of the grid
     * @param row The row of the grid
     * @param pollutionValue The pollution value of the cell
     * @param minPollutionValue The minimum pollution value for the map
     * @param colorRange The range of color gradients based on pollution levels
     */
    public void setCanvasCell(int col, int row, double pollutionValue, double minPollutionValue, double colorRange) {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        Color cellColor = getCellColor(pollutionValue, minPollutionValue, colorRange, LOW_OPACITY);
        gc.setFill(cellColor); // Set the fill color
        gc.fillRect(col * GRID_SIZE, row * GRID_SIZE, GRID_SIZE, GRID_SIZE); // Draw the cell
    }
    
    /**
     * Handles the selection of a pollutant from the combo box and updates
     * the current pollutant in the map manager.
     * 
     * @param event The action event triggered by selecting a pollutant
     */
    private void selectedPollutant(ActionEvent event) {
        String selectedPollutant = pollutantComboBox.getValue();
        if (selectedPollutant != null) {
            mapManager.setCurrentPollutant(selectedPollutant); // Set the selected pollutant
            mapManager.rewriteCurrentFileName(); // Update the filename based on selected pollutant
        }
    }
    
    /**
     * Handles the selection of a year from the combo box and updates
     * the current year in the map manager.
     * 
     * @param event The action event triggered by selecting a year
     */
    private void selectedYear(ActionEvent event) {
        String selectedYear = yearComboBox.getValue();
        if (selectedYear != null) {
            mapManager.setCurrentYear(selectedYear); // Set the selected year
            mapManager.rewriteCurrentFileName(); // Update the filename based on selected year
        }
    }
    
    /**
     * Handles mouse clicks on grid cells and displays information about the clicked cell.
     * 
     * @param event The mouse event triggered by clicking a grid cell
     */
    private void cellClicked(MouseEvent event) {
        int cellX = (int) event.getX() / GRID_SIZE;
        int cellY = (int) event.getY() / GRID_SIZE;
        
        // Only process the click if the cell is within bounds
        if (cellX < COLUMNS && cellY < ROWS) {
            DataPoint clickedDataPoint = mapManager.getGridDataPoint(cellX, cellY);
            
            if (clickedDataPoint != null) {
                // Get the clicked cell's information
                int gridCode = clickedDataPoint.gridCode();
                int col = clickedDataPoint.x();
                int row = clickedDataPoint.y();
                double pollutionValue = clickedDataPoint.value();
                double minPollutionValue = mapManager.getMinPollutionValue();
                double colorRange = mapManager.getColorRange();
                
                // Update the labels with information about the clicked cell
                gridCodeLabel.setText(GRID_CODE_LABEL_PREFIX + gridCode);
                xLabel.setText(X_LABEL_PREFIX + col);
                yLabel.setText(Y_LABEL_PREFIX + row);
                valueLabel.setText(VALUE_LABEL_PREFIX + pollutionValue + " µg/m³");
                
                // Get the graphics context for the canvas
                GraphicsContext gc = gridCanvas.getGraphicsContext2D();
                
                // If there's a previous clicked cell, reset its color
                DataPoint prevClickedDataPoint = mapManager.getPrevClickedDataPoint();
                if (prevClickedDataPoint != null && prevClickedDataPoint != clickedDataPoint) {
                    double prevPollutionValue = prevClickedDataPoint.value();
                    int prevCellX = mapManager.getPrevCellX();
                    int prevCellY = mapManager.getPrevCellY();
                    
                    // Clear the previous cell
                    gc.clearRect(prevCellX * GRID_SIZE, prevCellY * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                    
                    // Redraw the previous cell
                    Color prevCellColor = getCellColor(prevPollutionValue, minPollutionValue, colorRange, LOW_OPACITY);
                    gc.setFill(prevCellColor);
                    gc.fillRect(prevCellX * GRID_SIZE, prevCellY * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                }
                
                // Draw the clicked cell
                Color cellColor = getCellColor(pollutionValue, minPollutionValue, colorRange, HIGH_OPACITY);
                gc.setFill(cellColor);
                gc.fillRect(cellX * GRID_SIZE, cellY * GRID_SIZE, GRID_SIZE, GRID_SIZE);
                
                // Store information about the clicked cell for future reference
                mapManager.setPrevClickedDataPoint(clickedDataPoint);
                mapManager.setPrevCellX(cellX);
                mapManager.setPrevCellY(cellY);
            }
        }
    }
    
    /**
     * Determines the color of a cell based on the pollution value, minimum pollution value, color range, and opacity.
     * 
     * @param pollutionValue The pollution value of the cell
     * @param minValue The minimum pollution value for the map
     * @param colorRange The range of color gradients
     * @param opacity The opacity for the cell color
     * @return The color for the cell based on the pollution value
     */
    private Color getCellColor(double pollutionValue, double minValue, double colorRange, double opacity) {
        if (pollutionValue <= minValue + colorRange) return Color.rgb(68, 206, 27, opacity);               // Lime Green
        else if (pollutionValue <= minValue + colorRange * 2) return Color.rgb(187,219,68, opacity);       // Yellow-Green
        else if (pollutionValue <= minValue + colorRange * 3) return Color.rgb(247, 227, 121, opacity);    // Pastel Yellow
        else if (pollutionValue <= minValue + colorRange * 4) return Color.rgb(242, 161, 52, opacity);     // Orange
        else return Color.rgb(229, 31, 31, opacity);                                                       // Bright Red
    }
}