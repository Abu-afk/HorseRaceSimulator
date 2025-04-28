/**
 * HorseBreed class represents different horse breeds with their specific attributes.
 * Each breed has different characteristics that affect racing performance.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class HorseBreed {
    // Constants for predefined breeds
    public static final HorseBreed THOROUGHBRED = new HorseBreed("Thoroughbred", 
            "Known for speed and agility, bred specifically for racing.", 
            1.2, 0.8, 0.9, 1.0);
    
    public static final HorseBreed ARABIAN = new HorseBreed("Arabian", 
            "Endurance and stamina, elegant with high spirit and intelligence.", 
            1.0, 1.1, 1.2, 0.9);
    
    public static final HorseBreed QUARTER_HORSE = new HorseBreed("Quarter Horse", 
            "Excels at sprinting short distances, strong and muscular.", 
            1.3, 0.7, 0.8, 1.1);
    
    public static final HorseBreed STANDARDBRED = new HorseBreed("Standardbred", 
            "Known for trotting ability and good temperament, versatile.", 
            0.9, 1.0, 1.0, 1.2);
    
    public static final HorseBreed APPALOOSA = new HorseBreed("Appaloosa", 
            "Versatile breed with distinctive spotted coat pattern.", 
            0.95, 0.95, 1.1, 1.0);
    
    public static final HorseBreed MUSTANG = new HorseBreed("Mustang", 
            "Wild and hardy horses with natural survival instincts.", 
            1.0, 1.2, 0.9, 0.8);
    
    public static final HorseBreed CLYDESDALE = new HorseBreed("Clydesdale", 
            "Draft horse, large and powerful but slower than racing breeds.", 
            0.7, 1.3, 0.8, 1.3);
    
    // Array of all available breeds for easy access
    public static final HorseBreed[] ALL_BREEDS = {
        THOROUGHBRED, ARABIAN, QUARTER_HORSE, STANDARDBRED, 
        APPALOOSA, MUSTANG, CLYDESDALE
    };
    
    // Fields
    private String name;            // Name of the breed
    private String description;     // Description of the breed
    private double speedFactor;     // How the breed affects speed (higher is faster)
    private double staminaFactor;   // How the breed affects stamina (ability to maintain speed)
    private double agilityFactor;   // How the breed affects ability to handle turns
    private double strengthFactor;  // How the breed affects resistance to falling
    
    /**
     * Constructor for HorseBreed
     * 
     * @param name The name of the breed
     * @param description Description of the breed's characteristics
     * @param speedFactor Factor affecting speed (1.0 is baseline)
     * @param staminaFactor Factor affecting stamina (1.0 is baseline)
     * @param agilityFactor Factor affecting agility (1.0 is baseline)
     * @param strengthFactor Factor affecting strength (1.0 is baseline)
     */
    public HorseBreed(String name, String description, double speedFactor, 
                     double staminaFactor, double agilityFactor, double strengthFactor) {
        this.name = name;
        this.description = description;
        this.speedFactor = speedFactor;
        this.staminaFactor = staminaFactor;
        this.agilityFactor = agilityFactor;
        this.strengthFactor = strengthFactor;
    }
    
    /**
     * Get the name of the breed
     * 
     * @return The breed name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the description of the breed
     * 
     * @return The breed description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the speed factor of the breed
     * 
     * @return The speed factor
     */
    public double getSpeedFactor() {
        return speedFactor;
    }
    
    /**
     * Get the stamina factor of the breed
     * 
     * @return The stamina factor
     */
    public double getStaminaFactor() {
        return staminaFactor;
    }
    
    /**
     * Get the agility factor of the breed
     * 
     * @return The agility factor
     */
    public double getAgilityFactor() {
        return agilityFactor;
    }
    
    /**
     * Get the strength factor of the breed
     * 
     * @return The strength factor
     */
    public double getStrengthFactor() {
        return strengthFactor;
    }
    
    /**
     * Find a breed by name
     * 
     * @param breedName The name to search for
     * @return The matching breed, or THOROUGHBRED if no match found
     */
    public static HorseBreed getBreedByName(String breedName) {
        for (HorseBreed breed : ALL_BREEDS) {
            if (breed.getName().equalsIgnoreCase(breedName)) {
                return breed;
            }
        }
        return THOROUGHBRED; // Default if not found
    }
    
    /**
     * String representation of the breed
     */
    @Override
    public String toString() {
        return name;
    }
}