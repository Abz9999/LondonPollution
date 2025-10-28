import java.util.Arrays;

/**
 * Handles the backend operations for the MapPanel.
 * The MapManager class is responsible for managing and displaying air pollution data 
 * on a grid-based map. It handles loading datasets, updating grid cells and tracking 
 * user interactions with the map.
 *
 * @author Patrick Dunham
 * @version 24.03.2025
 */
public class MapManager
{
    private MapPanel mapPanel; // The panel responsible for displaying the pollution data on the map
    
    private String currentFileName; // The current file name used to load the dataset
    private String currentPollutant; // The selected pollutant type (e.g. "no2", "pm25", "pm10")
    private String currentYear; // The selected year for the dataset.
    private DataSet currentDataSet; // The dataset currently loaded and displayed on the map
    private double minPollutionValue; // The minimum pollution value in the dataset
    private double maxPollutionValue; // The maximum pollution value in the dataset
    private double colorRange; // The range of values used to determine color gradients on the map
    private DataPoint prevClickedDataPoint; // The last data point clicked by the user
    private int prevCellX; // The X-coordinate of the previously clicked grid cell
    private int prevCellY; // The Y-coordinate of the previously clicked grid cell
    
    private final int COLUMNS; // The number of columns in the grid (determined by the map panel)
    private final int ROWS; // The number of rows in the grid (determined by the map panel)
    private DataPoint[][] grid; // A 2D array representing the grid of data points on the map
    
    private static final GridData GRID_DATA = new GridData(); // Contains information about the grid layout
    private static final int GRID_RESOLUTION = 1000; // Constant representing the resolution (1x1km) for mapping coordinates on the grid
    private static final int MISSING_VALUE = -1; // Represents a data point missing its pollution value (value set to -1)

    /**
     * Constructor for objects of class MapManager
     * 
     * @param mapPanel The panel responsible for displaying the pollution data on the map
     */
    public MapManager(MapPanel mapPanel)
    {
        this.mapPanel = mapPanel;
        COLUMNS = mapPanel.getColumns();
        ROWS = mapPanel.getRows();
        grid = new DataPoint[COLUMNS][ROWS];
    }

    /**
     * Resets all instance variables to their default initial values
     * This method clears the current pollution data and the grid tracking variables
     */
    public void resetVariables() {
        currentFileName = "";
        currentPollutant = "";
        currentYear = "";
        currentDataSet = null;
        minPollutionValue = 0;
        maxPollutionValue = 0;
        colorRange = 0;
        prevClickedDataPoint = null;
        prevCellX = -1;
        prevCellY = -1;
    }
    
    /**
     * Resets the grid by setting all cells to null
     */
    public void resetGrid() {
        for (int i = 0; i < grid.length; i++) {
            Arrays.fill(grid[i], null);
        }
    }
    
    /**
     * Retrieves the DataPoint object at the specified grid coordinates
     * 
     * @param cellX The X-coordinate of the grid cell
     * @param cellY The Y-coordinate of the grid cell
     * @return The DataPoint object stored at the specified grid coordinates
     */
    public DataPoint getGridDataPoint(int cellX, int cellY) {
        return grid[cellX][cellY];
    }
    
    /**
     * Retrieves the X-coordinate of the previously clicked grid cell
     * 
     * @return The X-coordinate of the previously clicked grid cell
     */
    public int getPrevCellX() {
        return prevCellX;
    }
    
    /**
     * Sets the X-coordinate of the previously clicked grid cell
     * 
     * @param prevCellX The X-coordinate to set for the previously clicked grid cell
     */
    public void setPrevCellX(int prevCellX) {
        this.prevCellX = prevCellX;
    }
    
    /**
     * Returns the Y-coordinate of the previously clicked grid cell
     * 
     * @return The Y-coordinate of the previously clicked grid cell
     */
    public int getPrevCellY() {
        return prevCellY;
    }
    
    /**
     * Sets the Y-coordinate of the previously clicked grid cell
     * 
     * @param prevCellY The Y-coordinate of the previously clicked grid cell
     */
    public void setPrevCellY(int prevCellY) {
        this.prevCellY = prevCellY;
    }
    
    /**
     * Returns the DataPoint object of the previously clicked grid cell
     * 
     * @return The DataPoint of the previously clicked grid cell
     */
    public DataPoint getPrevClickedDataPoint() {
        return prevClickedDataPoint;
    }
    
    /**
     * Sets the DataPoint for the previously clicked grid cell
     * 
     * @param prevClickedDataPoint The DataPoint object representing the cell that was clicked
     */
    public void setPrevClickedDataPoint(DataPoint prevClickedDataPoint) {
        this.prevClickedDataPoint = prevClickedDataPoint;
    }
    
    /**
     * Returns the minimum pollution value for the current dataset
     * 
     * @return The minimum pollution value.
     */
    public double getMinPollutionValue() {
        return minPollutionValue;
    }
    
    /**
     * Returns the color range used for mapping pollution values to colors
     * 
     * @return The color range
     */
    public double getColorRange() {
        return colorRange;
    }
    
    /**
     * Sets the current pollutant
     * 
     * @param pollutant The name of the pollutant
     */
    public void setCurrentPollutant(String pollutant) {
        currentPollutant = pollutant;
    }
    
    /**
     * Sets the current year
     * 
     * @param year The year to be set
     */
    public void setCurrentYear(String year) {
        currentYear = year;
    }
    
    /**
     * Updates the current file name based on the selected pollutant and year,
     * then loads and displays the corresponding data.
     * 
     * @throws NullPointerException if the file cannot be loaded or data is invalid
     */
    public void rewriteCurrentFileName() {
        currentFileName = "UKAirPollutionData/";
        
        if ("no2".equals(currentPollutant)) {
            currentFileName += String.format("NO2/map%s%s.csv", currentPollutant, currentYear);
        }
        else if ("pm25".equals(currentPollutant)) {
            currentFileName += String.format("pm2.5/map%s%sg.csv", currentPollutant, currentYear);
        }
        else if ("pm10".equals(currentPollutant)) {
            currentFileName += String.format("pm10/map%s%sg.csv", currentPollutant, currentYear);
        }
        
        try {
            DataLoader loader = new DataLoader();
            currentDataSet = loader.loadDataFile(currentFileName, true); // the London data set
            
            mapPanel.clearGrid();
            fillGridCells();
        }
        catch (NullPointerException e) {
            System.out.println("Null value encountered with file: " + currentFileName);
        }
    }
    
    /**
     * Fills and colors the map grid with data points based on the current dataset
     */
    public void fillGridCells() {
        minPollutionValue = currentDataSet.getMinPollutionValue();
        maxPollutionValue = currentDataSet.getMaxPollutionValue();
        colorRange = (maxPollutionValue - minPollutionValue) / 5; // split range into 5 different colors
        
        for (DataPoint dataPoint : currentDataSet.getData()) {
            int[] coordinates = getGridCoordinates(dataPoint);
            int col = coordinates[0];
            int row = coordinates[1];
            
            if (col >= 0 && col < COLUMNS && row >= 0 && row < ROWS) {
                grid[col][row] = dataPoint;
                double pollutionValue = dataPoint.value();
                
                // Only add a color to a grid cell if its pollution value is not "MISSING" (i.e. -1)
                if (pollutionValue != MISSING_VALUE) mapPanel.setCanvasCell(col, row, pollutionValue, minPollutionValue, colorRange);
            }
        }
    }
    
    /**
     * Converts the coordinates of a DataPoint (easting and northing) 
     * to grid coordinates (column and row) based on the grid resolution and the 
     * grid boundaries.
     * 
     * @param dataPoint The DataPoint containing the coordinates
     * @return An int array containing a column and row index
     */
    private int[] getGridCoordinates(DataPoint dataPoint) {
        int easting = dataPoint.x();
        int northing = dataPoint.y();
        
        // Get the grid boundaries from GRID_DATA
        int MAX_LEFT = GRID_DATA.getMaxLeft();
        int MAX_TOP = GRID_DATA.getMaxTop();
        
        // Calculate the grid column and row using the easting and northing values
        int col = (easting - MAX_LEFT) / GRID_RESOLUTION; // Column is based on easting
        int row = (MAX_TOP - northing) / GRID_RESOLUTION; // Row is based on northing (subtracting because northing increases downward)
        
        // Return the coordinates as an array (column, row)
        return new int[]{col, row};
    }
}