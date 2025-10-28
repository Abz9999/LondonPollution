/**
 * This class represents the maximum top, bottom, left and right positions
 * on the map, and provides mapping from Easting and Northing to Latitude and Longitude.
 *
 * @author Gor Vardanyan
 * @version 24.03.2025
 */
public class GridData {
    private final int MAX_LEFT = 510394;
    private final int MAX_RIGHT = 553297;
    private final int MAX_TOP = 193305;
    private final int MAX_BOTTOM = 168504;

    // Corresponding latitude and longitude ranges based on the corner points
    private static final double MIN_LATITUDE = 51.395246;  // At MAX_BOTTOM
    private static final double MAX_LATITUDE = 51.627741;  // At MAX_TOP
    private static final double MIN_LONGITUDE = -0.40653443;  // At MAX_LEFT
    private static final double MAX_LONGITUDE = 0.20205370;  // At MAX_RIGHT
    
    /**
     * Represents a geographical point with Latitude and Longitude.
     */
    public static class LatLon {
        private final double latitude;
        private final double longitude;

        public LatLon(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return String.format("Lat: %.6f, Lon: %.6f", latitude, longitude);
        }
    }

    /**
     * Maps an Easting and Northing coordinate to Latitude and Longitude using linear interpolation.
     *
     * @param easting  The Easting coordinate (X)
     * @param northing The Northing coordinate (Y)
     * @return A LatLon object containing the corresponding Latitude and Longitude
     */
    public LatLon mapToLatLon(double easting, double northing) {
        // Clamps the coordinates to the boundary range to avoid extrapolation
        double clampedEasting = Math.max(MAX_LEFT, Math.min(MAX_RIGHT, easting));
        double clampedNorthing = Math.max(MAX_BOTTOM, Math.min(MAX_TOP, northing));

        // Calculates (interpolates) latitude based on Northing
        double latitude = MIN_LATITUDE + (clampedNorthing - MAX_BOTTOM) * (MAX_LATITUDE - MIN_LATITUDE)
                / (MAX_TOP - MAX_BOTTOM);

        // Calculates longitude based on Easting
        double longitude = MIN_LONGITUDE + (clampedEasting - MAX_LEFT) * (MAX_LONGITUDE - MIN_LONGITUDE)
                / (MAX_RIGHT - MAX_LEFT);

        return new LatLon(latitude, longitude);
    }

    /**
     * Checks if the given Easting and Northing coordinates are within the defined region.
     *
     * @param easting  The Easting coordinate (X)
     * @param northing The Northing coordinate (Y)
     * @return True if the coordinates are within the region, false otherwise
     */
    public boolean isWithinRegion(double easting, double northing) {
        return easting >= MAX_LEFT && easting <= MAX_RIGHT &&
               northing >= MAX_BOTTOM && northing <= MAX_TOP;
    }
    
    /**
     * A getter method for maximum value of top for London
     */
    public int getMaxTop() {
        return MAX_TOP;
    }

    /**
     * A getter method for maximum value of bottom for London
     */
    public int getMaxBottom() {
        return MAX_BOTTOM;
    }

    /**
     * A getter method for maximum value of left for London
     */
    public int getMaxLeft() {
        return MAX_LEFT;
    }

    /**
     * A getter method for maximum value of right for London
     */
    public int getMaxRight() {
        return MAX_RIGHT;
    }
}