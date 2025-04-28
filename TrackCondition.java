/**
 * TrackCondition class represents the current condition of a track.
 * It affects horse performance through various factors.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class TrackCondition {
    // Constants for predefined track conditions
    public static final TrackCondition DRY = new TrackCondition("Dry", 1.0, 1.0, 0.05);
    public static final TrackCondition MUDDY = new TrackCondition("Muddy", 0.7, 0.9, 0.1);
    public static final TrackCondition ICY = new TrackCondition("Icy", 0.8, 0.7, 0.15);
    public static final TrackCondition WET = new TrackCondition("Wet", 0.85, 0.95, 0.08);
    public static final TrackCondition WINDY = new TrackCondition("Windy", 0.9, 1.0, 0.07);
    
    // Fields for track condition properties
    private String name;
    private double speedFactor;     // How the condition affects speed (1.0 = normal, <1.0 = slower)
    private double gripFactor;      // How good the grip is (1.0 = perfect, <1.0 = slippery)
    private double fallProbability; // Base probability for falling in this condition
    
    /**
     * Constructor for TrackCondition
     * 
     * @param conditionName The name of the condition
     * @param speedEffect How the condition affects speed (1.0 = no effect, <1.0 = slows down)
     * @param gripEffect How the condition affects grip (1.0 = no effect, <1.0 = reduces grip)
     * @param fallProb Base probability of falling under this condition
     */
    public TrackCondition(String conditionName, double speedEffect, double gripEffect, double fallProb) {
        this.name = conditionName;
        this.speedFactor = speedEffect;
        this.gripFactor = gripEffect;
        this.fallProbability = fallProb;
    }
    
    /**
     * Get the name of the track condition
     * 
     * @return The name of the condition
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the speed factor for this condition
     * 
     * @return The speed factor
     */
    public double getSpeedFactor() {
        return speedFactor;
    }
    
    /**
     * Get the grip factor for this condition
     * 
     * @return The grip factor
     */
    public double getGripFactor() {
        return gripFactor;
    }
    
    /**
     * Get the base probability of falling for this condition
     * 
     * @return The fall probability
     */
    public double getFallProbability() {
        return fallProbability;
    }
    
    /**
     * Calculate the actual probability of a horse falling based on
     * track condition, horse confidence, and other factors
     * 
     * @param horseConfidence The confidence of the horse (0.0 to 1.0)
     * @param curveFactor How curved the track is at this point (1.0 = straight, <1.0 = curved)
     * @param turnHandling The horse's ability to handle turns (0.0 to 1.0)
     * @return The probability of the horse falling
     */
    public double calculateFallProbability(double horseConfidence, double curveFactor, double turnHandling) {
        // Base probability from the track condition
        double baseProbability = fallProbability;
        
        // Higher confidence increases fall risk (fast but less stable)
        double confidenceEffect = horseConfidence;
        
        // Curves increase fall risk, but good turn handling mitigates this
        double curveEffect = (1.0 - curveFactor) * (1.0 - turnHandling);
        
        // Calculate final probability
        // Base + confidence effect + curve effect, all modified by grip factor
        double finalProbability = (baseProbability + (0.1 * confidenceEffect) + (0.2 * curveEffect)) / gripFactor;
        
        // Cap at reasonable values
        return Math.min(0.5, Math.max(0.01, finalProbability));
    }
    
    /**
     * String representation of the track condition
     */
    @Override
    public String toString() {
        return name;
    }
}