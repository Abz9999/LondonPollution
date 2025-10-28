import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * This will be the first window that the users will see when they start 
 * the application. They will be presented with a title, a button to help
 * them understand the application, a button to load the map, a button to load the statistics
 * and a button to load the grid data.
 *
 * @author Frankie Cole
 * @version 24.03.2025
 */
public class WelcomePanel extends Application
{
    // fields
    private Button helpButton;
    private Button mapButton;
    private Button statsButton;
    private Button gridDataButton;
    private Stage stage;

    /**
     * The start method is the main entry point for every JavaFX application. 
     * It is called after the init() method has returned and after 
     * the system is ready for the application to begin running.
     *
     * @param  stage the primary stage for this application.
     */
    @Override
    public void start(Stage stage)
    {
        this.stage = stage;
        // Create window and GUI
        GridPane root = initialiseRoot();
        showWelcomePanel(root);
        
        // check for button clicks
        helpButton.setOnAction(this::helpButtonPressed);
        mapButton.setOnAction(this::mapButtonPressed);
        statsButton.setOnAction(this::statsButtonPressed);
        gridDataButton.setOnAction(this::gridDataButtonPressed);
        
        // JavaFX must have a Scene (window content) inside a Stage (window)
        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("London Air Pollution");
        scene.getStylesheets().add("mystyle.css");
        stage.setScene(scene); 

        // Show the Stage (window)
        stage.show();
    }
    
    /**
     * This method will create the root of the scene which will
     * be of type Gridpane. Adds the needed rows and columns.
     *
     * @return Returns the gridPane
     */
    private GridPane initialiseRoot() {
        // initilasing the grid
        GridPane root = new GridPane();
        
        // Add a single column that all nodes will be in so they are alligned vertically
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(100);
        
        // create 4 rows so each node can be in its own square
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(25);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(25);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(25);
        RowConstraints row4 = new RowConstraints();
        row4.setPercentHeight(25);
        RowConstraints row5 = new RowConstraints();
        row5.setPercentHeight(25);
        
        // add the rows and columns to the gridPane
        root.getColumnConstraints().addAll(column1);
        root.getRowConstraints().addAll(row1, row2, row3, row4, row5);
        
        //root.setGridLinesVisible(true); // lets us see the grid
        
        return root;
    }

    /**
     * Adds all the nodes to the root in the specified loactions.
     *
     * @param root The Pane to which the components will be added.
     */
    private void showWelcomePanel(Pane root) {
        // Create the nodes
        Label title = new Label("London Pollutant Levels");
        title.setId("title");
        
        helpButton = new Button("Information about application");
        mapButton = new Button("Go to Map");
        statsButton = new Button("Go to Statistics");
        gridDataButton = new Button("Go to Grid Data");
        
        // Add the nodes to specified grid location
        GridPane.setConstraints(title, 0, 0);
        GridPane.setHalignment(title, javafx.geometry.HPos.CENTER);
        
        GridPane.setConstraints(helpButton, 0, 1);
        GridPane.setHalignment(helpButton, javafx.geometry.HPos.CENTER);
        
        GridPane.setConstraints(mapButton, 0, 2);
        GridPane.setHalignment(mapButton, javafx.geometry.HPos.CENTER);
        
        GridPane.setConstraints(statsButton, 0, 3);
        GridPane.setHalignment(statsButton, javafx.geometry.HPos.CENTER);
        
        GridPane.setConstraints(gridDataButton, 0, 4);
        GridPane.setHalignment(gridDataButton, javafx.geometry.HPos.CENTER);
        
        root.getChildren().addAll(title, helpButton, mapButton, statsButton, gridDataButton);
    }
    
    /**
     * Will take user to the window that will explain the application to them.
     * 
     * @param event The ActionEvent triggered by pressing the help button.
     */
    private void helpButtonPressed(ActionEvent event) {
        InformationPanel informationPanel = new InformationPanel();
        Stage informationPanelStage = new Stage();
        
        // Start and display the Information Panel
        informationPanel.start(informationPanelStage);
        informationPanelStage.show();
        
        // Close the current window
        stage.close();
    }
    
    /**
     * Will take user to the window that contains the London map.
     * 
     * @param event The ActionEvent triggered by pressing the map button.
     */
    private void mapButtonPressed(ActionEvent event) {
        MapPanel mapViewer = new MapPanel();
        Stage mapViewerStage = new Stage();
        
        // Start and display the Map Viewer
        mapViewer.start(mapViewerStage);
        mapViewerStage.show();
        
        // Close the current window
        stage.close();
    }
    
    /**
     * Will take user to the window that contains the air pollution statistics.
     * 
     * @param event The ActionEvent triggered by pressing the statistics button.
     */
    private void statsButtonPressed(ActionEvent event) {
        StatisticsPanel statisticsPanel = new StatisticsPanel();
        Stage statisticsPanelStage = new Stage();
        
        // Start and display the Statistics Panel
        statisticsPanel.start(statisticsPanelStage);
        statisticsPanelStage.show();
        
        // Close the current window
        stage.close();
    }
    
    /**
     * Will take user to the window that shows the main focus of the application
     * 
     * @param mapViewer Creates a new window that we will switch too
     * @param mapViewerStage Creates a stage to be put in the window
     */
    private void gridDataButtonPressed(ActionEvent event) {
        GridDataPanel gridDataPanel = new GridDataPanel();
        Stage gridDataPanelStage = new Stage();
        
        // Start and display the Grid Data Panel
        gridDataPanel.start(gridDataPanelStage);
        gridDataPanelStage.show();
        
        // Close the current window
        stage.close();
    }
}
