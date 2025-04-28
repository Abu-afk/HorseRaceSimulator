import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Track class represents the racing track with its shape, conditions, and properties.
 * It handles the track layout and calculations for horse positions on different track shapes.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public abstract class Track {
    // Fields for track properties
    protected int length;               // Track length
    protected int lanes;                // Number of lanes
    protected TrackCondition condition; // Current track condition
    protected String name;              // Track name
    protected List<Point2D.Double> trackPoints;  // Points defining the track path
    
    /**
     * Constructor for the Track class
     * 
     * @param trackName Name of the track
     * @param trackLength Length of the track
     * @param numberOfLanes Number of lanes on the track
     * @param trackCondition Initial track condition
     */
    public Track(String trackName, int trackLength, int numberOfLanes, TrackCondition trackCondition) {
        this.name = trackName;
        this.length = trackLength;
        this.lanes = numberOfLanes;
        this.condition = trackCondition;
        this.trackPoints = new ArrayList<>();
        generateTrackPath();
    }
    
    /**
     * Generate the track path based on its shape
     * This is an abstract method that must be implemented by subclasses
     */
    protected abstract void generateTrackPath();
    
    /**
     * Calculate the position of a horse on the track based on its distance traveled
     * 
     * @param distance The distance traveled by the horse
     * @param lane The lane number the horse is in (0-based)
     * @return A Point2D.Double representing the x,y coordinates of the horse
     */
    public abstract Point2D.Double calculatePosition(double distance, int lane);
    
    /**
     * Calculate the curve factor at a specific position on the track
     * 1.0 means straight, lower values indicate turns
     * 
     * @param distance The distance along the track
     * @return A value between 0.0 and 1.0 indicating how curved the track is at that point
     */
    public abstract double getCurveFactor(double distance);
    
    /**
     * Get the total length of the track
     * 
     * @return The track length
     */
    public int getLength() {
        return length;
    }
    
    /**
     * Get the number of lanes on the track
     * 
     * @return The number of lanes
     */
    public int getLanes() {
        return lanes;
    }
    
    /**
     * Get the current track condition
     * 
     * @return The current track condition
     */
    public TrackCondition getCondition() {
        return condition;
    }
    
    /**
     * Set a new track condition
     * 
     * @param newCondition The new track condition
     */
    public void setCondition(TrackCondition newCondition) {
        this.condition = newCondition;
    }
    
    /**
     * Get the track name
     * 
     * @return The name of the track
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set a new track length
     * 
     * @param newLength The new track length
     */
    public void setLength(int newLength) {
        this.length = newLength;
        generateTrackPath(); // Regenerate the track path
    }
    
    /**
     * Set a new number of lanes
     * 
     * @param newLanes The new number of lanes
     */
    public void setLanes(int newLanes) {
        if (newLanes > 0) {
            this.lanes = newLanes;
        }
    }
    
    /**
     * Get the list of points defining the track path
     * 
     * @return The list of points
     */
    public List<Point2D.Double> getTrackPoints() {
        return trackPoints;
    }
    
    /**
     * Check if a horse has completed the race
     * 
     * @param distanceTravelled The distance traveled by the horse
     * @return true if the race is completed, false otherwise
     */
    public boolean isRaceCompleted(double distanceTravelled) {
        return distanceTravelled >= length;
    }

    /**
     * Convert a relative distance (0.0 to 1.0) to an absolute track position
     * 
     * @param relativeDistance A value between 0.0 and 1.0 representing a position along the track
     * @return The corresponding absolute distance
     */
    protected double relativeToAbsoluteDistance(double relativeDistance) {
        return relativeDistance * length;
    }
    
    /**
     * Convert an absolute distance to a relative position (0.0 to 1.0)
     * 
     * @param absoluteDistance The absolute distance along the track
     * @return A value between 0.0 and 1.0 representing the relative position
     */
    protected double absoluteToRelativeDistance(double absoluteDistance) {
        return (absoluteDistance % length) / length;
    }
}