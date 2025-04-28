import java.awt.geom.Point2D;

/**
 * This file contains implementations of different track shapes.
 * Each class extends the Track base class and implements its own
 * track path generation and position calculation.
 * 
 * @author (Your Name)
 * @version (1.0)
 */

/**
 * OvalTrack represents a standard oval racing track
 */
class OvalTrack extends Track {
    private double width;  // Width of the oval
    private double height; // Height of the oval
    
    /**
     * Constructor for OvalTrack
     */
    public OvalTrack(String name, int length, int lanes, TrackCondition condition) {
        super(name, length, lanes, condition);
        this.width = length / 3.0;  // Width is 1/3 of the total track length
        this.height = length / 6.0; // Height is 1/6 of the total track length
    }
    
    @Override
    protected void generateTrackPath() {
        trackPoints.clear();
        
        // Generate points along the oval path
        int numberOfPoints = 100; // Number of points to generate
        
        for (int i = 0; i < numberOfPoints; i++) {
            double t = (double) i / numberOfPoints;
            double angle = 2 * Math.PI * t;
            
            double x = width * Math.cos(angle);
            double y = height * Math.sin(angle);
            
            trackPoints.add(new Point2D.Double(x, y));
        }
    }
    
    @Override
    public Point2D.Double calculatePosition(double distance, int lane) {
        // Convert distance to a relative position on the track (0.0 to 1.0)
        double relativeDistance = absoluteToRelativeDistance(distance);
        
        // Calculate angle based on relative distance
        double angle = 2 * Math.PI * relativeDistance;
        
        // Calculate lane offset (outer lanes have larger radius)
        double laneOffset = lane * 10; // 10 pixels between lanes
        
        // Calculate position
        double x = (width + laneOffset) * Math.cos(angle);
        double y = (height + laneOffset) * Math.sin(angle);
        
        return new Point2D.Double(x, y);
    }
    
    @Override
    public double getCurveFactor(double distance) {
        // In an oval, curves are at the top and bottom
        double relativeDistance = absoluteToRelativeDistance(distance);
        double angle = 2 * Math.PI * relativeDistance;
        
        // Calculate how curved the track is at this point (1.0 = straight, 0.5 = most curved)
        // For an oval, we use sine to determine curvature (highest at 0, PI, 2PI, etc.)
        double curveFactor = 0.5 + 0.5 * Math.abs(Math.sin(angle));
        
        return curveFactor;
    }
}

/**
 * FigureEightTrack represents a figure-8 shaped track with a crossing in the middle
 */
class FigureEightTrack extends Track {
    private double width;  // Width of each loop
    private double height; // Height of each loop
    
    /**
     * Constructor for FigureEightTrack
     */
    public FigureEightTrack(String name, int length, int lanes, TrackCondition condition) {
        super(name, length, lanes, condition);
        this.width = length / 5.0;  // Width of each loop
        this.height = length / 10.0; // Height of each loop
    }
    
    @Override
    protected void generateTrackPath() {
        trackPoints.clear();
        
        // Generate points along the figure-8 path
        int numberOfPoints = 200; // More points for complex shape
        
        for (int i = 0; i < numberOfPoints; i++) {
            double t = (double) i / numberOfPoints;
            double angle = 2 * Math.PI * t;
            
            // Figure-8 parametric equations
            double x = width * Math.sin(angle);
            double y = height * Math.sin(2 * angle);
            
            trackPoints.add(new Point2D.Double(x, y));
        }
    }
    
    @Override
    public Point2D.Double calculatePosition(double distance, int lane) {
        // Convert distance to a relative position on the track (0.0 to 1.0)
        double relativeDistance = absoluteToRelativeDistance(distance);
        
        // Calculate angle based on relative distance
        double angle = 2 * Math.PI * relativeDistance;
        
        // Figure-8 parametric equations
        double x = width * Math.sin(angle);
        double y = height * Math.sin(2 * angle);
        
        // Calculate perpendicular direction for lane offset
        double dx = width * Math.cos(angle);
        double dy = 2 * height * Math.cos(2 * angle);
        
        // Normalize the perpendicular vector
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len > 0) {
            dx /= len;
            dy /= len;
        }
        
        // Perpendicular direction (swap and negate)
        double perpX = -dy;
        double perpY = dx;
        
        // Apply lane offset
        double laneOffset = lane * 8; // 8 pixels between lanes
        x += perpX * laneOffset;
        y += perpY * laneOffset;
        
        return new Point2D.Double(x, y);
    }
    
    @Override
    public double getCurveFactor(double distance) {
        // In a figure-8, the crossing point has the sharpest turn
        double relativeDistance = absoluteToRelativeDistance(distance);
        double angle = 2 * Math.PI * relativeDistance;
        
        // Calculate curvature based on position in the figure-8
        // The crossing in the middle is the sharpest turn
        // Crossing occurs at angles near 0, PI, 2PI, etc.
        double crossingFactor = Math.abs(Math.sin(angle));
        
        // Also consider the general curvature of the track
        double generalCurvature = 0.5 + 0.3 * Math.abs(Math.cos(2 * angle));
        
        // Combine the factors, with crossing having more weight
        double curveFactor = 0.3 * crossingFactor + 0.7 * generalCurvature;
        
        // Ensure the curve factor stays within bounds
        return Math.max(0.3, Math.min(1.0, curveFactor));
    }
    
    /**
     * Check if a horse is at the crossing point of the figure-8
     * 
     * @param distance The distance traveled by the horse
     * @return true if the horse is at the crossing, false otherwise
     */
    public boolean isAtCrossing(double distance) {
        double relativeDistance = absoluteToRelativeDistance(distance);
        double angle = 2 * Math.PI * relativeDistance;
        
        // Crossing is when x and y are close to 0 (center)
        return Math.abs(Math.sin(angle)) < 0.1 && Math.abs(Math.sin(2 * angle)) < 0.1;
    }
}

/**
 * ZigzagTrack represents a track with sharp turns
 */
class ZigzagTrack extends Track {
    private int segments; // Number of zigzag segments
    
    /**
     * Constructor for ZigzagTrack
     */
    public ZigzagTrack(String name, int length, int lanes, TrackCondition condition) {
        super(name, length, lanes, condition);
        this.segments = 6; // Number of zigzag segments
    }
    
    @Override
    protected void generateTrackPath() {
        trackPoints.clear();
        
        double segmentWidth = length / (2.0 * segments);
        double segmentHeight = length / (4.0 * segments);
        
        // Generate zigzag path
        for (int i = 0; i <= segments; i++) {
            double x = i * 2 * segmentWidth;
            double y = (i % 2 == 0) ? 0 : segmentHeight;
            
            trackPoints.add(new Point2D.Double(x, y));
        }
    }
    
    @Override
    public Point2D.Double calculatePosition(double distance, int lane) {
        // Convert distance to a relative position on the track (0.0 to 1.0)
        double relativeDistance = absoluteToRelativeDistance(distance);
        
        double segmentWidth = length / (2.0 * segments);
        double segmentHeight = length / (4.0 * segments);
        
        // Calculate which segment the horse is in
        int segment = (int)(relativeDistance * segments);
        double segmentProgress = (relativeDistance * segments) - segment;
        
        // Calculate position based on segment
        double x, y;
        
        if (segment % 2 == 0) {
            // Even segment (going up)
            x = segment * 2 * segmentWidth + (2 * segmentWidth * segmentProgress);
            y = segmentHeight * segmentProgress;
        } else {
            // Odd segment (going down)
            x = (segment * 2 * segmentWidth) + (2 * segmentWidth * segmentProgress);
            y = segmentHeight * (1 - segmentProgress);
        }
        
        // Apply lane offset perpendicular to the current segment direction
        double laneOffset = lane * 8; // 8 pixels between lanes
        
        if (segment % 2 == 0) {
            // Even segment (going up) - lanes spread horizontally
            y += laneOffset;
        } else {
            // Odd segment (going down) - lanes spread horizontally
            y += laneOffset;
        }
        
        return new Point2D.Double(x, y);
    }
    
    @Override
    public double getCurveFactor(double distance) {
        // In a zigzag track, turns are at the segment boundaries
        double relativeDistance = absoluteToRelativeDistance(distance);
        
        // Calculate which segment the horse is in
        double segmentRelative = relativeDistance * segments;
        double segmentProgress = segmentRelative - Math.floor(segmentRelative);
        
        // Turns are at the beginning and end of segments
        // Calculate distance from nearest turn
        double distanceFromTurn = Math.min(segmentProgress, 1.0 - segmentProgress);
        
        // Convert to a curve factor (1.0 = straight, 0.2 = sharp turn)
        // Linear interpolation between sharp turns (0.2) and straight segments (1.0)
        double curveFactor = 0.2 + 0.8 * Math.min(1.0, distanceFromTurn * 5);
        
        return curveFactor;
    }
}