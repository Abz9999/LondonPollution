import java.util.*;

/**
 * A DataSet object holds all the data from a pollution data file.
 * 
 * It is assumed that the data is derived from a DEFRA air pollution file (see
 * https://uk-air.defra.gov.uk/data/pcm-data). 
 * 
 * The data consists of a few bits of information about the nature of the data, and a list
 * of data points.
 * 
 * @author Michael Kölling and Patrick Dunham
 * @version 24.03.2025
 */
public class DataSet
{
    private String pollutant;
    private String year;
    private String metric;
    private String units;
    
    private List<DataPoint> data;
    private double minPollutionValue;
    private double maxPollutionValue;
    private static final int MISSING_VALUE = -1; // Represents a data point missing its pollution value (value set to -1)

    /**
     * Constructor for objects of class DataSet
     */
    public DataSet(String pollutant, String year, String metric, String units)
    {
        this.pollutant = pollutant;
        this.year = year;
        this.metric = metric;
        this.units = units;
        
        data = new ArrayList<DataPoint>();
        minPollutionValue = Double.MAX_VALUE;
        maxPollutionValue = 0;
    }

    /**
     * Return the pollutant information for this dataset.
     */
    public String getPollutant()
    {
        return pollutant;
    }
    
    /**
     * Return the year information for this dataset.
     */
    public String getYear()
    {
        return year;
    }
    
    /**
     * Return the metric information for this dataset.
     */
    public String getMetric()
    {
        return metric;
    }
    
    /**
     * Return the units information for this dataset.
     */
    public String getUnits()
    {
        return units;
    }
    
    /**
     * Return the lowest pollution value for this dataset.
     */
    public double getMinPollutionValue() {
        return minPollutionValue;
    }
    
    /**
     * Return the highest pollution value for this dataset.
     */
    public double getMaxPollutionValue() {
        return maxPollutionValue;
    }
    
    /**
     * Return the data points of this dataset.
     */
    public List<DataPoint> getData()
    {
        return data;
    }
    
    /**
     * Set data to the given list of data points.
     * 
     * @param dataPoints A list of data points
     */
    public void setData(List<DataPoint> dataPoints) {
        data = dataPoints;
    }
    
    /**
     * Add a data point to this dataset. 
     * A data point consists of 4 pieces of data:
     * 
     *     gridcode, x, y, value
     *     
     * The data is provided in a String array of length 4. If the value is invalid or
     * missing, it will be stored as -1.
     * 
     * After adding the data point, the minimum and maximum pollution values are updated.
     *
     * @param  values  An array with the four data values (as Strings)
     */
    public void addData(String[] values)
    {
        double pollutionValue = toDouble(values[3]);
        data.add(new DataPoint(toInt(values[0]), 
                               toInt(values[1]), 
                               toInt(values[2]), 
                               pollutionValue));
        
        setMinPollutionValue(pollutionValue);
        setMaxPollutionValue(pollutionValue);
    }
    
    /**
     * Sets the minimum pollution value.
     * 
     * @param pollutionValue The pollution value to be checked and potentially set as the new minimum.
     */
    private void setMinPollutionValue(double pollutionValue) {
        if (pollutionValue < minPollutionValue && pollutionValue != MISSING_VALUE) minPollutionValue = pollutionValue;
    }
    
    /**
     * Sets the maximum pollution value.
     * 
     * @param pollutionValue The pollution value to be checked and potentially set as the new maximum.
     */
    private void setMaxPollutionValue(double pollutionValue) {
        if (pollutionValue > maxPollutionValue && pollutionValue != MISSING_VALUE) maxPollutionValue = pollutionValue;
    }
    
    /**
     * Convert a string to int. 
     * @param intString  The String holding the int value
     * @return  The int value, or -1 if the string is not a readable number
     */
    private int toInt(String intString)
    {
        try {
            return Integer.parseInt(intString);
        }
        catch (NumberFormatException exc) {
            return MISSING_VALUE;
        }
    }

    /**
     * Convert a string to double.
     * @param doubleString  The String holding the double value
     * @return  The double value, or -1.0 if the string is not a readable number
     */
    private double toDouble(String doubleString)
    {
        try {
            return Double.parseDouble(doubleString);
        }
        catch (NumberFormatException exc) {
            return MISSING_VALUE;
        }
    }

    /**
     * Return a string representation of this dataset info.
     */
    public String toString()
    {
        return String.format("Dataset: Pollutant: %s, Year: %s, Metric: %s, Units: %s (%d data points)",
                             pollutant, year, metric, units, data.size());
    }
}