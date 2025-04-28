/**
 * 
 * 
 * 
 * @author (Original: Abu,version)
 * @version (version 3.0)
 */
public class Horse
{
    // Basic fields
    private String name;           // Name of the horse
    private char symbol;           // Symbol representing the horse
    private String displaySymbol;  // Visual representation (emoji or special character)
    private double distanceTravelled; // Distance the horse has traveled (now a double for more precise movement)
    private double xPosition;      // X coordinate on 2D track
    private double yPosition;      // Y coordinate on 2D track
    private boolean fallen;        // Whether the horse has fallen
    
    // Attributes
    private double confidence;     // Confidence rating (0.0 to 1.0)
    private double speed;          // Current speed of the horse
    private double baseSpeed;      // Base speed of the horse (can be adjusted by track conditions)
    private double turnHandling;   // How well the horse handles turns (0.0 to 1.0)
    private double stamina;        // How well the horse maintains speed over time (0.0 to 1.0)
    private double luck;           // Random factor affecting performance (0.0 to 1.0)
    
    // Customization fields
    private HorseBreed breed;      // The breed of the horse
    private CoatColor coatColor;   // The coat color
    private HorseEquipment equipment; // The horse's equipment
    
    /**
     * Basic constructor for objects of class Horse (for backward compatibility)
     * 
     * @param horseSymbol The character used to represent the horse
     * @param horseName The name of the horse
     * @param horseConfidence The confidence rating of the horse (0.0 to 1.0)
     */
    public Horse(char horseSymbol, String horseName, double horseConfidence)
    {
        this.symbol = horseSymbol;
        this.name = horseName;
        this.confidence = horseConfidence;
        this.distanceTravelled = 0;
        this.xPosition = 0;
        this.yPosition = 0;
        this.fallen = false;
        this.baseSpeed = 0.5 + (confidence * 0.5); // Speed influenced by confidence
        this.speed = baseSpeed;
        this.turnHandling = Math.random() * 0.5 + 0.5; // Random handling ability between 0.5 and 1.0
        this.stamina = 0.7;
        this.luck = Math.random();
        
        // Default customization
        this.breed = HorseBreed.THOROUGHBRED;
        this.coatColor = CoatColor.BAY;
        this.equipment = new HorseEquipment();
        this.displaySymbol = String.valueOf(horseSymbol); // Default to the character symbol
    }
    
    /**
     * Alternative constructor with more detailed attributes (for backward compatibility)
     */
    public Horse(char horseSymbol, String horseName, double horseConfidence, 
                 double horseTurnHandling, double horseBaseSpeed) {
        this.symbol = horseSymbol;
        this.name = horseName;
        this.confidence = horseConfidence;
        this.distanceTravelled = 0;
        this.xPosition = 0;
        this.yPosition = 0;
        this.fallen = false;
        this.baseSpeed = horseBaseSpeed;
        this.speed = baseSpeed;
        this.turnHandling = horseTurnHandling;
        this.stamina = 0.7;
        this.luck = Math.random();
        
        // Default customization
        this.breed = HorseBreed.THOROUGHBRED;
        this.coatColor = CoatColor.BAY;
        this.equipment = new HorseEquipment();
        this.displaySymbol = String.valueOf(horseSymbol); // Default to the character symbol
    }
    
    /**
     * Full constructor with all customization options
     */
    public Horse(char horseSymbol, String horseName, String displaySymbol, 
                 double horseConfidence, HorseBreed breed, CoatColor coatColor, 
                 HorseEquipment equipment) {
        this.symbol = horseSymbol;
        this.name = horseName;
        this.displaySymbol = displaySymbol;
        this.confidence = horseConfidence;
        this.distanceTravelled = 0;
        this.xPosition = 0;
        this.yPosition = 0;
        this.fallen = false;
        
        this.breed = breed;
        this.coatColor = coatColor;
        this.equipment = equipment;
        
        // Calculate base attributes based on breed and equipment
        calculateBaseAttributes();
        
        // Set initial speed
        this.speed = baseSpeed;
    }
    
    /**
     * Calculate the horse's base attributes based on breed and equipment
     */
    private void calculateBaseAttributes() {
        // Base speed calculation
        double baseSpeedValue = 0.5 + (confidence * 0.5);
        baseSpeedValue *= breed.getSpeedFactor();
        baseSpeedValue *= equipment.getSpeedFactor();
        this.baseSpeed = baseSpeedValue;
        
        // Turn handling calculation
        double baseTurnHandling = 0.5 + (Math.random() * 0.3);
        baseTurnHandling *= breed.getAgilityFactor();
        baseTurnHandling *= equipment.getStabilityFactor();
        this.turnHandling = Math.min(1.0, baseTurnHandling);
        
        // Stamina calculation
        double baseStamina = 0.6 + (Math.random() * 0.3);
        baseStamina *= breed.getStaminaFactor();
        baseStamina *= equipment.getEnduranceFactor();
        this.stamina = Math.min(1.0, baseStamina);
        
        // Luck calculation
        this.luck = Math.random() * equipment.getLuckFactor();
    }
    
    /**
     * Makes the horse fall during the race
     */
    public void fall()
    {
        this.fallen = true;
    }
    
    /**
     * Returns the confidence rating of the horse
     * 
     * @return The confidence rating value (between 0.0 and 1.0)
     */
    public double getConfidence()
    {
        return this.confidence;
    }
    
    /**
     * Returns the distance traveled by the horse
     * 
     * @return The distance traveled in race units
     */
    public double getDistanceTravelled()
    {
        return this.distanceTravelled;
    }
    
    /**
     * Returns the integer distance (for backward compatibility)
     */
    public int getDistanceAsInt() {
        return (int)this.distanceTravelled;
    }
    
    /**
     * Returns the name of the horse
     * 
     * @return The horse's name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Returns the symbol used to represent the horse
     * 
     * @return The character symbol for the horse
     */
    public char getSymbol()
    {
        return this.symbol;
    }
    
    /**
     * Returns the display symbol (emoji or special character)
     * 
     * @return The display symbol
     */
    public String getDisplaySymbol() {
        return this.displaySymbol;
    }
    
    /**
     * Get the current X position of the horse
     */
    public double getXPosition() {
        return xPosition;
    }
    
    /**
     * Get the current Y position of the horse
     */
    public double getYPosition() {
        return yPosition;
    }
    
    /**
     * Get the current speed of the horse
     */
    public double getSpeed() {
        return speed;
    }
    
    /**
     * Get the base speed of the horse
     */
    public double getBaseSpeed() {
        return baseSpeed;
    }
    
    /**
     * Get the turn handling ability of the horse
     */
    public double getTurnHandling() {
        return turnHandling;
    }
    
    /**
     * Get the stamina of the horse
     */
    public double getStamina() {
        return stamina;
    }
    
    /**
     * Get the luck factor of the horse
     */
    public double getLuck() {
        return luck;
    }
    
    /**
     * Get the breed of the horse
     */
    public HorseBreed getBreed() {
        return breed;
    }
    
    /**
     * Get the coat color of the horse
     */
    public CoatColor getCoatColor() {
        return coatColor;
    }
    
    /**
     * Get the equipment of the horse
     */
    public HorseEquipment getEquipment() {
        return equipment;
    }
    
    /**
     * Set the position of the horse on a 2D track
     */
    public void setPosition(double x, double y) {
        this.xPosition = x;
        this.yPosition = y;
    }
    
    /**
     * Set the current speed of the horse
     */
    public void setSpeed(double newSpeed) {
        this.speed = newSpeed;
    }
    
    /**
     * Adjust the horse's speed by a factor
     * 
     * @param factor The multiplication factor to apply to current speed
     */
    public void adjustSpeed(double factor) {
        this.speed *= factor;
    }
    
    /**
     * Set the display symbol for the horse
     * 
     * @param displaySymbol The new display symbol
     */
    public void setDisplaySymbol(String displaySymbol) {
        this.displaySymbol = displaySymbol;
    }
    
    /**
     * Set the breed of the horse and recalculate attributes
     * 
     * @param breed The new breed
     */
    public void setBreed(HorseBreed breed) {
        this.breed = breed;
        calculateBaseAttributes();
    }
    
    /**
     * Set the coat color of the horse
     * 
     * @param coatColor The new coat color
     */
    public void setCoatColor(CoatColor coatColor) {
        this.coatColor = coatColor;
    }
    
    /**
     * Set the equipment of the horse and recalculate attributes
     * 
     * @param equipment The new equipment
     */
    public void setEquipment(HorseEquipment equipment) {
        this.equipment = equipment;
        calculateBaseAttributes();
    }

    public void updateMovement(double trackConditionFactor, double curveFactor, double distanceFactor) {
        if (!fallen) {
            // Apply stamina effect (horses slow down as race progresses)
            double staminaEffect = 1.0 - ((1.0 - stamina) * distanceFactor);
            
            // Adjust for turn handling ability based on breed and equipment
            double turnEffect = 1.0 - ((1.0 - curveFactor) * (1.0 - turnHandling));
            
            // Calculate movement increment
            double increment = speed * trackConditionFactor * turnEffect * staminaEffect;
            
            // Apply random luck factor (small random boost or penalty)
            double luckFactor = 0.95 + (luck * 0.1); // Between 0.95 and 1.05
            increment *= luckFactor;
            
            // Update distance
            distanceTravelled += increment;
        }
    }
    
    /**
     * Overloaded method for backward compatibility
     */
    public void updateMovement(double trackConditionFactor, double curveFactor) {
        // Assume we're halfway through the race if not specified
        updateMovement(trackConditionFactor, curveFactor, 0.5);
    }
    
    /**
     * Resets the horse to the start of the race
     * Horse is no longer fallen and distance is set to zero
     */
    public void goBackToStart()
    {
        this.distanceTravelled = 0;
        this.xPosition = 0;
        this.yPosition = 0;
        this.fallen = false;
        this.speed = baseSpeed;
    }
    
    /**
     * Checks if the horse has fallen during the race
     * 
     * @return true if the horse has fallen, false otherwise
     */
    public boolean hasFallen()
    {
        return this.fallen;
    }

    /**
     * Moves the horse forward one unit of distance
     * Only moves if the horse hasn't fallen
     * (Legacy method for backward compatibility)
     */
    public void moveForward()
    {
        if (!this.fallen) {
            this.distanceTravelled += speed;
        }
    }

    /**
     * Updates the confidence rating of the horse
     * 
     * @param newConfidence The new confidence value (should be between 0.0 and 1.0)
     */
    public void setConfidence(double newConfidence)
    {
        // Ensure confidence stays within valid range
        if (newConfidence < 0.0) {
            this.confidence = 0.0;
        } else if (newConfidence > 1.0) {
            this.confidence = 1.0;
        } else {
            this.confidence = newConfidence;
        }
        
        // Update base speed since it's influenced by confidence
        calculateBaseAttributes();
    }
    
    /**
     * Changes the symbol used to represent the horse
     * 
     * @param newSymbol The new character symbol
     */
    public void setSymbol(char newSymbol)
    {
        this.symbol = newSymbol;
    }
    
    /**
     * Set the turn handling ability of the horse
     * 
     * @param newTurnHandling The new turn handling value (0.0 to 1.0)
     */
    public void setTurnHandling(double newTurnHandling) {
        if (newTurnHandling < 0.0) {
            this.turnHandling = 0.0;
        } else if (newTurnHandling > 1.0) {
            this.turnHandling = 1.0;
        } else {
            this.turnHandling = newTurnHandling;
        }
    }
    
    /**
     * Set the stamina of the horse
     * 
     * @param newStamina The new stamina value (0.0 to 1.0)
     */
    public void setStamina(double newStamina) {
        if (newStamina < 0.0) {
            this.stamina = 0.0;
        } else if (newStamina > 1.0) {
            this.stamina = 1.0;
        } else {
            this.stamina = newStamina;
        }
    }
    
    /**
     * Update the saddle of the horse
     * 
     * @param saddleType The new saddle type
     */
    public void updateSaddle(HorseEquipment.SaddleType saddleType) {
        equipment.setSaddle(saddleType);
        calculateBaseAttributes();
    }
    
    /**
     * Update the horseshoes of the horse
     * 
     * @param horseshoeType The new horseshoe type
     */
    public void updateHorseshoes(HorseEquipment.HorseshoeType horseshoeType) {
        equipment.setHorseshoes(horseshoeType);
        calculateBaseAttributes();
    }
    
    /**
     * Update the accessory of the horse
     * 
     * @param accessoryType The new accessory type
     */
    public void updateAccessory(HorseEquipment.AccessoryType accessoryType) {
        equipment.setAccessory(accessoryType);
        calculateBaseAttributes();
    }
    
    /**
     * Returns a string representation of the horse
     */
    @Override
    public String toString() {
        return name + " (" + displaySymbol + ") - " + breed.getName() + ", " + coatColor.getName() + "\n" +
               "Speed: " + String.format("%.2f", baseSpeed) + 
               ", Stamina: " + String.format("%.2f", stamina) + 
               ", Turn Handling: " + String.format("%.2f", turnHandling) + "\n" +
               "Equipment: " + equipment.toString();
    }
    
    /**
     * Get a detailed statistics string for the horse
     */
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== " + name + " ===\n");
        stats.append("Breed: " + breed.getName() + "\n");
        stats.append("Color: " + coatColor.getName() + "\n");
        stats.append("Confidence: " + String.format("%.2f", confidence) + "\n");
        stats.append("Base Speed: " + String.format("%.2f", baseSpeed) + "\n");
        stats.append("Turn Handling: " + String.format("%.2f", turnHandling) + "\n");
        stats.append("Stamina: " + String.format("%.2f", stamina) + "\n");
        stats.append("Luck Factor: " + String.format("%.2f", luck) + "\n");
        stats.append("\nEquipment:\n");
        stats.append("- Saddle: " + equipment.getSaddle().getName() + "\n");
        stats.append("- Horseshoes: " + equipment.getHorseshoes().getName() + "\n");
        stats.append("- Accessory: " + equipment.getAccessory().getName() + "\n");
        return stats.toString();
    }
}
