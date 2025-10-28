import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;

/**
 * This is a window that will explain how the application works to the user
 *
 * @author Abdallah Batah
 * @version 24.03.2025
 */
public class InformationPanel extends Application
{
    // fields
    private Stage stage;
    private Button backButton;
    
    private Label applicationName;
    private Label version;
    private Label authors;
    private Label description;
    
    /**
     * The start method is the main entry point for every JavaFX application. 
     * It is called after the init() method has returned and after 
     * the system is ready for the application to begin running.
     *
     * @param  stage the primary stage for this application.
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        
        // Add a back button
        HBox backButtonContainer = new HBox();
        addBackButton(backButtonContainer);
        backButton.setOnAction(this::backPressed); // check for button press
        
        // Add the title
        HBox titleContainer = new HBox();        
        addTitleLabel(titleContainer);
        
        //  Add the application descriptions
        VBox informationBox = new VBox(10);
        addDescription(informationBox);

        // put the descriptions and title together
        VBox infoLayout = new VBox(20, titleContainer, informationBox);
        infoLayout.setPadding(new Insets(20));

        // Main Layout (BorderPane)
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(backButtonContainer);
        mainLayout.setCenter(infoLayout);

        // Scene and Stage setup
        Scene scene = new Scene(mainLayout, 1000, 600);        
        scene.getStylesheets().add("mystyle.css");
        
        stage.setTitle("Information Panel");
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Creates a button that gets added to a HBox 
     * 
     * @param backButton A button that when clicked will take the user back to the 
     * home page
     */
    private void addBackButton(HBox backButtonContainer) {
        backButton = new Button("←Back");

        backButtonContainer.getChildren().add(backButton);
        backButtonContainer.setAlignment(Pos.TOP_LEFT);
        backButtonContainer.setPadding(new Insets(10));
    }
    
    /**
     * Creates a label that contains the title of this window. It also styles the
     * label as well.
     * 
     * @param titleLabel Label that contains the windows title
     */
    private void addTitleLabel(HBox titleContainer) {
        Label titleLabel = new Label("Application Information");
        titleLabel.setId("title");
        
        titleContainer.getChildren().add(titleLabel);
        titleContainer.setAlignment(Pos.CENTER);
        titleContainer.setPadding(new Insets(10));
    }
    
    /**
     * This method creates adds to a VBox multiple HBox's that each contain some 
     * label which has a different description of the application.
     *
     * 
     * @param nameLabel Applications name
     * @param versionLabel Version of the application
     * @param authorLabel Authors of the application
     * @param descriptionLabel Description explaining the application
     * @param nameBox HBox containing nameLabel and names value
     * @param versionBox HBox containing versionLabel and version value
     * @param authorBox HBox containing authorLabel and authors value
     * @param descriptionBox HBox containing descriptionLabel and description value
     */
    private void addDescription(VBox informationBox) {
        // Create labels needed
        Label nameLabel = new Label("Application Name: ");
        nameLabel.setMinWidth(100);
        Label versionLabel = new Label("Version: ");
        versionLabel.setMinWidth(100);
        Label authorLabel = new Label("Authors: ");
        authorLabel.setMinWidth(100);
        Label descriptionLabel = new Label("Description: "); 
        descriptionLabel.setMinWidth(100);

        // add each label to its own HBox along with its value
        HBox nameBox = new HBox(10, nameLabel, getApplicationNameValue());
        nameBox.setAlignment(Pos.CENTER_LEFT);

        HBox versionBox = new HBox(10, versionLabel, getVersionValue());
        versionBox.setAlignment(Pos.CENTER_LEFT);

        HBox authorBox = new HBox(10, authorLabel, getAuthorsValue());
        authorBox.setAlignment(Pos.CENTER_LEFT);

        HBox descriptionBox = new HBox(10, descriptionLabel, getDescriptionValue());
        descriptionBox.setAlignment(Pos.TOP_LEFT);
        
        // add each HBox into a VBox
        informationBox.getChildren().addAll(nameBox, versionBox, authorBox, descriptionBox);
    }
    
    /**
     * This method returns the applicationName value
     */
    private Label getApplicationNameValue() {
        applicationName = new Label("London Air Pollution");
        
        return applicationName;
    }
    
    /**
     * This method returns the Version value
     */
    private Label getVersionValue() {
        version = new Label("24.03.2025");
        
        return version;
    }
    
    /**
     * This method returns the authors value
     */
    private Label getAuthorsValue() {
        authors = new Label(
            "Abdallah Batah, Frankie Cole, Gor Vardanyan and Patrick Dunham"
        );
        
        return authors;
    }
    
    /**
     * This method returns the description value
     */
    private Label getDescriptionValue() {
        description = new Label("""
        The application visualizes and analyzes air pollution data for London. It allows users to explore pollution levels (NO2, PM10, PM2.5) over different years. The data is displayed on a map with color-coded markers for pollution levels, and users can view detailed statistics and trends.
        """);
        
        description.setWrapText(true);
        description.setMaxWidth(400); 
        
        return description;
    }
    
    /**
     * This method will bring the user back to the home window when the back button
     * is pressed
     */
    private void backPressed(ActionEvent event) {
        System.out.println("back pressed");
        WelcomePanel welcomePanel = new WelcomePanel();
        Stage welcomePanelStage = new Stage();
        welcomePanel.start(welcomePanelStage);
        welcomePanelStage.show();
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
