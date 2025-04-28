/**
 * HorseEquipment class represents the various equipment options for horses.
 * This includes saddles, horseshoes, and other accessories that affect performance.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class HorseEquipment {
    // Saddle types
    public enum SaddleType {
        RACING("Racing Saddle", "Lightweight saddle designed for speed", 1.1, 0.95),
        ENGLISH("English Saddle", "Traditional saddle with balanced performance", 1.0, 1.0),
        WESTERN("Western Saddle", "Heavier saddle with better stability", 0.9, 1.1),
        BAREBACK("Bareback", "No saddle, lighter but less stable", 1.15, 0.85),
        DRESSAGE("Dressage Saddle", "Precise control but not designed for speed", 0.95, 1.05);
        
        private String name;
        private String description;
        private double speedFactor;
        private double stabilityFactor;
        
        SaddleType(String name, String description, double speedFactor, double stabilityFactor) {
            this.name = name;
            this.description = description;
            this.speedFactor = speedFactor;
            this.stabilityFactor = stabilityFactor;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getSpeedFactor() { return speedFactor; }
        public double getStabilityFactor() { return stabilityFactor; }
        
        @Override
        public String toString() { return name; }
    }
    
    // Horseshoe types
    public enum HorseshoeType {
        STANDARD("Standard Horseshoes", "Balanced performance on various tracks", 1.0, 1.0, 1.0),
        LIGHTWEIGHT("Lightweight Horseshoes", "Improves speed but less durable", 1.1, 0.9, 0.95),
        TRACTION("Traction Horseshoes", "Better grip on slippery surfaces", 0.95, 1.15, 1.1),
        THERAPEUTIC("Therapeutic Horseshoes", "Designed for comfort and endurance", 0.9, 1.05, 1.2),
        NONE("No Horseshoes", "Natural hoof, better on soft ground", 1.05, 0.95, 0.9);
        
        private String name;
        private String description;
        private double speedFactor;
        private double gripFactor;
        private double enduranceFactor;
        
        HorseshoeType(String name, String description, double speedFactor, double gripFactor, double enduranceFactor) {
            this.name = name;
            this.description = description;
            this.speedFactor = speedFactor;
            this.gripFactor = gripFactor;
            this.enduranceFactor = enduranceFactor;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getSpeedFactor() { return speedFactor; }
        public double getGripFactor() { return gripFactor; }
        public double getEnduranceFactor() { return enduranceFactor; }
        
        @Override
        public String toString() { return name; }
    }
    
    // Accessory types
    public enum AccessoryType {
        NONE("No Accessories", "No additional accessories", 0.0, 0.0),
        BLINDERS("Blinders", "Reduces distractions, improves focus", 0.05, 0.0),
        BLANKET("Racing Blanket", "Streamlines for slightly better speed", 0.03, 0.0),
        PLUME("Decorative Plume", "Fancy but offers no performance benefit", 0.0, 0.0),
        LUCKY_CHARM("Lucky Charm", "May bring good fortune", 0.0, 0.05),
        PERFORMANCE_BRIDLE("Performance Bridle", "Improves control", 0.0, 0.03);
        
        private String name;
        private String description;
        private double speedBoost;
        private double luckBoost;
        
        AccessoryType(String name, String description, double speedBoost, double luckBoost) {
            this.name = name;
            this.description = description;
            this.speedBoost = speedBoost;
            this.luckBoost = luckBoost;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getSpeedBoost() { return speedBoost; }
        public double getLuckBoost() { return luckBoost; }
        
        @Override
        public String toString() { return name; }
    }
    
    // Fields
    private SaddleType saddle;
    private HorseshoeType horseshoes;
    private AccessoryType accessory;
    
    /**
     * Constructor for HorseEquipment
     */
    public HorseEquipment() {
        // Default equipment
        this.saddle = SaddleType.RACING;
        this.horseshoes = HorseshoeType.STANDARD;
        this.accessory = AccessoryType.NONE;
    }
    
    /**
     * Constructor with specified equipment
     * 
     * @param saddle The saddle type
     * @param horseshoes The horseshoe type
     * @param accessory The accessory type
     */
    public HorseEquipment(SaddleType saddle, HorseshoeType horseshoes, AccessoryType accessory) {
        this.saddle = saddle;
        this.horseshoes = horseshoes;
        this.accessory = accessory;
    }
    
    /**
     * Get the saddle type
     * 
     * @return The saddle type
     */
    public SaddleType getSaddle() {
        return saddle;
    }
    
    /**
     * Set the saddle type
     * 
     * @param saddle The new saddle type
     */
    public void setSaddle(SaddleType saddle) {
        this.saddle = saddle;
    }
    
    /**
     * Get the horseshoe type
     * 
     * @return The horseshoe type
     */
    public HorseshoeType getHorseshoes() {
        return horseshoes;
    }
    
    /**
     * Set the horseshoe type
     * 
     * @param horseshoes The new horseshoe type
     */
    public void setHorseshoes(HorseshoeType horseshoes) {
        this.horseshoes = horseshoes;
    }
    
    /**
     * Get the accessory type
     * 
     * @return The accessory type
     */
    public AccessoryType getAccessory() {
        return accessory;
    }
    
    /**
     * Set the accessory type
     * 
     * @param accessory The new accessory type
     */
    public void setAccessory(AccessoryType accessory) {
        this.accessory = accessory;
    }
    
    /**
     * Calculate the combined speed factor from all equipment
     * 
     * @return The combined speed factor
     */
    public double getSpeedFactor() {
        double factor = saddle.getSpeedFactor() * horseshoes.getSpeedFactor();
        factor += accessory.getSpeedBoost();
        return factor;
    }
    
    /**
     * Calculate the combined stability/grip factor from all equipment
     * 
     * @return The combined stability factor
     */
    public double getStabilityFactor() {
        return saddle.getStabilityFactor() * horseshoes.getGripFactor();
    }
    
    /**
     * Calculate the endurance factor from equipment
     * 
     * @return The endurance factor
     */
    public double getEnduranceFactor() {
        return horseshoes.getEnduranceFactor();
    }
    
    /**
     * Get the luck factor from accessories
     * 
     * @return The luck factor
     */
    public double getLuckFactor() {
        return 1.0 + accessory.getLuckBoost();
    }
    
    /**
     * String representation of the equipment
     */
    @Override
    public String toString() {
        return "Saddle: " + saddle.getName() + 
               ", Horseshoes: " + horseshoes.getName() + 
               ", Accessory: " + accessory.getName();
    }
}