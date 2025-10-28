import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DataSets is a singleton class that stores two HashMaps of type DataSet (UK and London).
 *
 * @author Patrick Dunham
 * @version 24.03.2025
 */
public class DataSets
{
    // Static variable reference of instance of type DataSets
    private static DataSets instance = null;
    
    private static HashMap<String, DataSet> allDataSets;
    private static HashMap<String, DataSet> londonDataSets;
    private static final GridData GRID_DATA = new GridData();
    
    /**
     * Constructor for objects of class DataSets
     */
    private DataSets()
    {
        // Initialise instance variables
        allDataSets = new HashMap<>();
        londonDataSets = new HashMap<>();
    }
    
    /**
     * Static method to create instance of Singleton class
     */
    public static DataSets getInstance()
    {
        if (instance == null) {        
            instance = new DataSets();
        }
        
        return instance;
    }
    
    /**
     * Returns the appropriate dataset HashMap based on the 'isLondon' flag.
     *
     * @param isLondon A boolean flag indicating whether to return London data or all data
     * @return The HashMap of datasets based on the region (UK or London)
     */
    public static HashMap<String, DataSet> getDataSets(boolean isLondon) {
        if (isLondon) return londonDataSets;
        
        return allDataSets;
    }
    
    /**
     * Puts a data set into the appropriate HashMap based on the 'isLondon' flag.
     * If the region is London, filters out DataPoints outside the region using GRID_DATA.
     *
     * @param fileName The filename associated with the DataSet
     * @param dataSet The DataSet to be added
     * @param isLondon A boolean flag indicating whether to filter London data or not
     */
    public static void putDataSets(String fileName, DataSet dataSet, boolean isLondon) {
        // If "isLondon" is true, filter out data points that are not in the London map
        if (isLondon) {
            List<DataPoint> filteredDataSet = dataSet.getData().stream()
                .filter(dataPoint -> GRID_DATA.isWithinRegion(dataPoint.x(), dataPoint.y()))
                .collect(Collectors.toList());
            
            dataSet.setData(filteredDataSet);
            londonDataSets.put(fileName, dataSet);
        }
        else {
            allDataSets.put(fileName, dataSet);
        }
    }
}