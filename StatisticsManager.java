import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * This class handles the maths and operations behind the statistics panel
 * based on the information collected from the provided database.
 * 
 * @author  Gor Vardanyan
 * @version 24.03.2025
 */

public class StatisticsManager {
    private List<DataSet> dataSets;
    private String selectedPollutant;
    private String selectedArea;
    private double averageLatitude;
    private double averageLongitude;

    /**
     * Constructor of the manager class.
     * @param dataSets - the data for a particular area
     * @param area - the name of the location
     */
    public StatisticsManager(List<DataSet> dataSets, String area) {
        this.dataSets = dataSets != null ? dataSets : new ArrayList<>();
        this.selectedPollutant = dataSets != null && !dataSets.isEmpty() ? dataSets.get(0).getPollutant() : "Unknown";
        this.selectedArea = area;
    }
    
    /**
     * Method for updating the statistics status for average level info. 
     */
    public void updateAverageStats(Label avgLevelLabel) {
        List<DataPoint> filteredData = getFilteredData();
        if (filteredData.isEmpty()) {
            avgLevelLabel.setText("Average Level: No data");
            averageLatitude = 0;
            averageLongitude = 0;
            return;
        }
        double totalLevel = 0;
        int count = 0;
        for (DataPoint dataPoint : filteredData) {
            if (dataPoint.value() >= 0) {
                totalLevel += dataPoint.value();
                count++;
            }
        }
        double avgLevel = (count > 0) ? totalLevel / count : 0;
        
        // Calculates the average Easting and Northing for the location
        double avgEasting = 0;
        double avgNorthing = 0;
        for (DataPoint dp : filteredData) {
            avgEasting += dp.x();
            avgNorthing += dp.y();
        }
        avgEasting = (count > 0) ? avgEasting / count : 0;
        avgNorthing = (count > 0) ? avgNorthing / count : 0;

        // Maps the average location to Latitude and Longitude
        GridData gridData = new GridData();
        GridData.LatLon latLon = gridData.mapToLatLon(avgEasting, avgNorthing);
        averageLatitude = latLon.getLatitude();
        averageLongitude = latLon.getLongitude();
        
        String units = dataSets.isEmpty() ? "ug m-3" : dataSets.get(0).getUnits();
        avgLevelLabel.setText(String.format("Average Level: %.2f %s", avgLevel, dataSets.get(0).getUnits()));
        
    }

    /**
     * Updates the Peak Levels tab with the highest recorded pollution levels,
     * including their Latitude and Longitude.
     */
        public void updatePeakStats(ListView<String> peakList) {
        List<DataPoint> filteredData = getFilteredData();
        if (filteredData.isEmpty()) {
            peakList.getItems().setAll("No data available");
            return;
        }
    
        // Removes duplicates by converting to a Set and back to a List
        Set<DataPoint> uniqueData = new HashSet<>(filteredData);
        List<DataPoint> sortedData = new ArrayList<>(uniqueData);
        
        // Sorts data points by value in descending order
        sortedData.removeIf(dp -> dp.value() < 0);
        sortedData.sort((dp1, dp2) -> Double.compare(dp2.value(), dp1.value()));
    
        // Creates a GridData instance
        GridData gd = new GridData();
    
        // Updates the ListView with peak data, including Lat/Lon
        peakList.getItems().clear();
        for (int i = 0; i < Math.min(3, sortedData.size()); i++) {
            DataPoint dp = sortedData.get(i);
            GridData.LatLon latLon = gd.mapToLatLon(dp.x(), dp.y());
            String entry = String.format("Grid %d: %.2f %s (Lat: %.6f, Lon: %.6f)",
                    dp.gridCode(), dp.value(), dataSets.get(0).getUnits(),
                    latLon.getLatitude(), latLon.getLongitude());
            peakList.getItems().add(entry);
        }
    }

    /**
     * Method for updating the statistics the graph of the trend as 
     * the given information changes by the user.
     */
    public void updateTrendsStats(XYChart<Number, Number> lineChart) {
        if (lineChart == null || dataSets.isEmpty()) {
            if (lineChart != null) {
                lineChart.getData().clear();
            }
            return;
        }
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(selectedPollutant + " Levels (" + selectedArea + ")");
        for (DataSet dataSet : dataSets) {
            List<DataPoint> filteredData = filterDataByArea(dataSet.getData());
            double avgLevel = getAverageForCurrentData(filteredData);
            series.getData().add(new XYChart.Data<>(Integer.parseInt(dataSet.getYear()), avgLevel));
        }
        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    /**
     * Getter method for filtered data.
     */
    protected List<DataPoint> getFilteredData() {
        List<DataPoint> allData = new ArrayList<>();
        for (DataSet dataSet : dataSets) {
            allData.addAll(filterDataByArea(dataSet.getData()));
        }
        return allData;
    }
    
    /**
     * A method for filtering the data by the area chosen by the user.
     */
    protected List<DataPoint> filterDataByArea(List<DataPoint> data) {
        List<DataPoint> filteredData = new ArrayList<>();
        GridData edge = new GridData();
        for (DataPoint dp : data) {
            if (selectedArea.equals("All") || 
                (selectedArea.equals("London") && dp.x() >= edge.getMaxLeft() && dp.x() <= edge.getMaxRight() && dp.y() >= edge.getMaxBottom() && dp.y() <= edge.getMaxTop())) {
                filteredData.add(dp);
            }
        }
        return filteredData;
    }

    /**
     * A method for finding the average pollution level for data.
     */
    protected double getAverageForCurrentData(List<DataPoint> data) {
        if (data.isEmpty()) return 0;
        double totalLevel = 0;
        int count = 0;
        for (DataPoint dataPoint : data) {
            if (dataPoint.value() >= 0) {
                totalLevel += dataPoint.value();
                count++;
            }
        }
        return (count > 0) ? totalLevel / count : 0;
    }
    
    /**
     * Setter method for new pollutants (used for the given ones).
     */
    public void setSelectedPollutant(String selectedPollutant) {
        this.selectedPollutant = selectedPollutant;
    }
    
    /**
     * Getter method for the latitude for the average tab.
     */
    public double getAverageLatitude() {
        return averageLatitude;
    }
    
    /**
     * Getter method for the longitude for the average tab.
     */
    public double getAverageLongitude() {
        return averageLongitude;
    }
    
    /**
     * Getter method for the selected pollutant
     */
    protected String getSelectedPollutant() {
        return selectedPollutant;
    }
    
    /**
     * Getter method for the selected area
     */
    protected String getSelectedArea() {
        return selectedArea;
    }
    
    /**
     * Getter metthod for the dataset
     */
    protected List<DataSet> getDataSets() {
        return dataSets;
    }
    
    /**
     * Method for getting the level of pollution for certain coordinates.
     */
    public double getPollutionLevelForCoordinates(double easting, double northing) {
        List<DataPoint> filteredData = getFilteredData();
        double tolerance = 1000.0;  // 1km tolerance
        DataPoint closest = null;
        double minDistance = Double.MAX_VALUE;
    
        for (DataPoint dp : filteredData) {
            double dx = dp.x() - easting;
            double dy = dp.y() - northing;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < minDistance && distance <= tolerance) {
                minDistance = distance;
                closest = dp;
            }
        }
        if (closest != null) {
            System.out.println("Found closest DataPoint at Easting=" + closest.x() + ", Northing=" + closest.y() + ", Distance=" + minDistance);
            return closest.value();
        }
        return -1;
}
}