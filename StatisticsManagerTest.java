import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the StatisticsManager class, verifying its core functionality 
 * in handling environmental data and updating JavaFX UI components.
 * 
 * @author Gor Vardanyan
 * @version 24.03.2025
 */
public class StatisticsManagerTest {

    private StatisticsManager statsManager;
    private List<DataSet> dataSets;
    private List<DataPoint> dataPoints;
    private Label avgLevelLabel;
    private ListView<String> peakList;
    private LineChart<Number, Number> lineChart;
    private GridData gridData;

    // Static flag to track JavaFX initialization
    private static boolean javaFXInitialized = false;

    @BeforeEach
    public void setUp() throws Exception {
        // Initializes JavaFX (only if not initialized before)
        if (!javaFXInitialized && !Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(() -> {
                    latch.countDown(); // Signals that JavaFX is ready
                });
                // Waits for JavaFX to initialize, with a 5-second timeout
                boolean initialized = latch.await(5, TimeUnit.SECONDS);
                if (!initialized) {
                    throw new IllegalStateException("JavaFX initialization timed out");
                }
                javaFXInitialized = true;
            } catch (IllegalStateException e) {
                // If toolkit is already initialized, it is assumed to be ready
                if (e.getMessage().contains("Toolkit already initialized")) {
                    javaFXInitialized = true;
                } else {
                    throw e;
                }
            }
        }

        // Creates test data points
        dataPoints = new ArrayList<>();
        dataPoints.add(new DataPoint(12345, 520000, 180000, 10.0));
        dataPoints.add(new DataPoint(67890, 530000, 190000, 20.0)); // Both within London bounds

        // Creates test data sets
        DataSet dataSet1 = new DataSet("NO2", "2023", "Annual Mean", "ug m-3");
        dataSet1.getData().addAll(dataPoints);
        DataSet dataSet2 = new DataSet("NO2", "2022", "Annual Mean", "ug m-3");
        dataSet2.getData().addAll(dataPoints);
        dataSets = Arrays.asList(dataSet1, dataSet2);

        // Initializes GridData
        gridData = new GridData();

        // Initializes StatisticsManager
        statsManager = new StatisticsManager(dataSets, "London");

        // Initializes JavaFX components
        avgLevelLabel = new Label();
        peakList = new ListView<>();
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        lineChart = new LineChart<>(xAxis, yAxis);
    }

    /**
     * Tests the StatisticsManager constructor with valid datasets.
     * Verifies pollutant, area, and dataset size are set correctly.
     */
    @Test
    public void testConstructor_WithValidDataSets() {
        assertEquals("NO2", statsManager.getSelectedPollutant());
        assertEquals("London", statsManager.getSelectedArea());
        assertEquals(2, statsManager.getDataSets().size());
    }

    /**
     * Tests the constructor with null datasets.
     * Ensures defaults to "Unknown" pollutant, specified area, and empty dataset list.
     */
    @Test
    public void testConstructor_WithNullDataSets() {
        StatisticsManager manager = new StatisticsManager(null, "All");
        assertEquals("Unknown", manager.getSelectedPollutant());
        assertEquals("All", manager.getSelectedArea());
        assertTrue(manager.getDataSets().isEmpty());
    }

    /**
     * Tests updateAverageStats with valid data.
     * Checks if average is calculated (15.0) and lat/lon are non-zero.
     */
    @Test
    public void testUpdateAverageStats_WithValidData() {
        statsManager.updateAverageStats(avgLevelLabel);

        // Expected average: (10.0 + 20.0) / 2 = 15.0 (two datasets, same data points)
        assertEquals("Average Level: 15.00 ug m-3", avgLevelLabel.getText());
        // Checks the computed average Lat/Lon
        assertNotEquals(0.0, statsManager.getAverageLatitude());  // Should be non-zero if GridData works
        assertNotEquals(0.0, statsManager.getAverageLongitude());
    }

    /**
     * Tests updateAverageStats with empty data.
     * Verifies "No data" message and zero lat/lon.
     */
    @Test
    public void testUpdateAverageStats_WithEmptyData() {
        statsManager = new StatisticsManager(new ArrayList<>(), "London");
        statsManager.updateAverageStats(avgLevelLabel);

        assertEquals("Average Level: No data", avgLevelLabel.getText());
        assertEquals(0.0, statsManager.getAverageLatitude());
        assertEquals(0.0, statsManager.getAverageLongitude());
    }

    /**
     * Tests updatePeakStats with valid data.
     * Ensures peaks (20.0, 10.0) are listed with grid IDs and lat/lon.
     */
    @Test
    public void testUpdatePeakStats_WithValidData() {
        statsManager.updatePeakStats(peakList);

        // Expected: two entries, sorted by value (20.0, 10.0)
        List<String> expected = new ArrayList<>();
        GridData.LatLon latLon = gridData.mapToLatLon(530000, 190000);
        expected.add(String.format("Grid 67890: 20.00 ug m-3 (Lat: %.6f, Lon: %.6f)",
                latLon.getLatitude(), latLon.getLongitude()));
        latLon = gridData.mapToLatLon(520000, 180000);
        expected.add(String.format("Grid 12345: 10.00 ug m-3 (Lat: %.6f, Lon: %.6f)",
                latLon.getLatitude(), latLon.getLongitude()));
        assertEquals(expected, peakList.getItems());
    }

    /**
     * Tests updatePeakStats with empty data.
     * Verifies "No data available" is displayed.
     */
    @Test
    public void testUpdatePeakStats_WithEmptyData() {
        statsManager = new StatisticsManager(new ArrayList<>(), "London");
        statsManager.updatePeakStats(peakList);

        assertEquals(Arrays.asList("No data available"), peakList.getItems());
    }

    /**
     * Tests updateTrendsStats with valid data.
     * Checks line chart has one series with two points (2022, 2023) at 15.0.
     */
    @Test
    public void testUpdateTrendsStats_WithValidData() {
        statsManager.setSelectedPollutant("NO2");
        statsManager.updateTrendsStats(lineChart);

        // Expected: two data points for 2022 and 2023, both with average 15.0
        assertEquals(1, lineChart.getData().size());
        XYChart.Series<Number, Number> series = lineChart.getData().get(0);
        assertEquals("NO2 Levels (London)", series.getName());
        assertEquals(2, series.getData().size());
        assertEquals(2023, series.getData().get(0).getXValue());
        assertEquals(15.0, series.getData().get(0).getYValue().doubleValue(), 0.01);
        assertEquals(2022, series.getData().get(1).getXValue());
        assertEquals(15.0, series.getData().get(1).getYValue().doubleValue(), 0.01);
    }

    /**
     * Tests updateTrendsStats with empty data.
     * Ensures the line chart remains empty.
     */
    @Test
    public void testUpdateTrendsStats_WithEmptyData() {
        statsManager = new StatisticsManager(new ArrayList<>(), "London");
        statsManager.updateTrendsStats(lineChart);

        assertTrue(lineChart.getData().isEmpty());
    }

    /**
     * Tests filterDataByArea for "London".
     * Verifies both data points are included (within bounds).
     */
    @Test
    public void testFilterDataByArea_London() {
        List<DataPoint> filtered = statsManager.filterDataByArea(dataPoints);
        assertEquals(2, filtered.size());  // Both points are within London bounds
        assertTrue(filtered.contains(dataPoints.get(0)));
        assertTrue(filtered.contains(dataPoints.get(1)));
    }

    /**
     * Tests filterDataByArea for "All".
     * Ensures all data points are included regardless of bounds.
     */
    @Test
    public void testFilterDataByArea_All() {
        statsManager = new StatisticsManager(dataSets, "All");
        List<DataPoint> filtered = statsManager.filterDataByArea(dataPoints);
        assertEquals(2, filtered.size());  // All points should be included
        assertTrue(filtered.contains(dataPoints.get(0)));
        assertTrue(filtered.contains(dataPoints.get(1)));
    }

    /**
     * Tests filterDataByArea with an out-of-bounds point.
     * Verifies only the in-bounds point is included.
     */
    @Test
    public void testFilterDataByArea_OutOfBounds() {
        // Creates a DataPoint outside London bounds
        DataPoint outOfBoundsPoint = new DataPoint(99999, 400000, 100000, 5.0);  // Below MAX_LEFT
        List<DataPoint> testData = Arrays.asList(dataPoints.get(0), outOfBoundsPoint);

        List<DataPoint> filtered = statsManager.filterDataByArea(testData);
        assertEquals(1, filtered.size());
        assertTrue(filtered.contains(dataPoints.get(0)));
        assertFalse(filtered.contains(outOfBoundsPoint));
    }

    /**
     * Tests getAverageForCurrentData with valid data.
     * Verifies average of 10.0 and 20.0 is 15.0.
     */
    @Test
    public void testGetAverageForCurrentData_WithValidData() {
        double average = statsManager.getAverageForCurrentData(dataPoints);
        assertEquals(15.0, average, 0.01);  // (10.0 + 20.0) / 2 = 15.0
    }

    /**
     * Tests getAverageForCurrentData with negative values.
     * Ensures negative values are ignored, averaging to 10.0.
     */
    @Test
    public void testGetAverageForCurrentData_WithNegativeValues() {
        List<DataPoint> testData = Arrays.asList(
            new DataPoint(12345, 520000, 180000, 10.0),
            new DataPoint(67890, 530000, 190000, -5.0)
        );
        double average = statsManager.getAverageForCurrentData(testData);
        assertEquals(10.0, average, 0.01);
    }

    /**
     * Tests getAverageForCurrentData with empty data.
     * Verifies it returns 0.0.
     */
    @Test
    public void testGetAverageForCurrentData_WithEmptyData() {
        double average = statsManager.getAverageForCurrentData(new ArrayList<>());
        assertEquals(0.0, average, 0.01);
    }
}