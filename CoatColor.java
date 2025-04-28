import java.awt.Color;

/**
 * CoatColor class represents the various coat colors available for horses.
 * Each coat color has a name and a corresponding color for display.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class CoatColor {
    // Constants for predefined coat colors
    public static final CoatColor BAY = new CoatColor("Bay", 
            "Reddish-brown coat with black mane, tail, and lower legs.", 
            new Color(153, 76, 0));
    
    public static final CoatColor BLACK = new CoatColor("Black", 
            "Pure black coat, mane, and tail.", 
            new Color(25, 25, 25));
    
    public static final CoatColor CHESTNUT = new CoatColor("Chestnut", 
            "Reddish-brown coat with same color or flaxen mane and tail.", 
            new Color(205, 92, 0));
    
    public static final CoatColor GRAY = new CoatColor("Gray", 
            "Mixture of white and dark hairs, appears silver-gray.", 
            new Color(180, 180, 180));
    
    public static final CoatColor WHITE = new CoatColor("White", 
            "Pure white coat, often with pink skin.", 
            new Color(250, 250, 250));
    
    public static final CoatColor PALOMINO = new CoatColor("Palomino", 
            "Golden coat with white or cream mane and tail.", 
            new Color(255, 215, 115));
    
    public static final CoatColor PINTO = new CoatColor("Pinto", 
            "Large patches of white and dark hair.", 
            new Color(200, 150, 100)); // Base color, pattern handled separately
    
    public static final CoatColor BUCKSKIN = new CoatColor("Buckskin", 
            "Golden or tan body with black points (mane, tail, legs).", 
            new Color(222, 184, 135));
    
    public static final CoatColor DAPPLE_GRAY = new CoatColor("Dapple Gray", 
            "Gray with distinctive rounded pattern of darker spots.", 
            new Color(169, 169, 169)); // Base color, pattern handled separately
    
    public static final CoatColor ROAN = new CoatColor("Roan", 
            "Mixture of white and colored hairs, giving a blended appearance.", 
            new Color(180, 150, 150)); // Base color, mixture handled separately
    
    // Array of all available coat colors for easy access
    public static final CoatColor[] ALL_COLORS = {
        BAY, BLACK, CHESTNUT, GRAY, WHITE, PALOMINO, PINTO, BUCKSKIN, DAPPLE_GRAY, ROAN
    };
    
    // Fields
    private String name;        // Name of the coat color
    private String description; // Description of the coat color
    private Color color;        // The actual color for display
    
    /**
     * Constructor for CoatColor
     * 
     * @param name The name of the coat color
     * @param description Description of the coat color
     * @param color The Java Color object for display
     */
    public CoatColor(String name, String description, Color color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }
    
    /**
     * Get the name of the coat color
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the description of the coat color
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the color for display
     * 
     * @return The Java Color object
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Find a coat color by name
     * 
     * @param colorName The name to search for
     * @return The matching coat color, or BAY if no match found
     */
    public static CoatColor getColorByName(String colorName) {
        for (CoatColor coatColor : ALL_COLORS) {
            if (coatColor.getName().equalsIgnoreCase(colorName)) {
                return coatColor;
            }
        }
        return BAY; // Default if not found
    }
    
    /**
     * String representation of the coat color
     */
    @Override
    public String toString() {
        return name;
    }
}