/**
 * Helper class to provide singleton access to the RaceManager.
 * This is used by BettingService to access the current race information.
 */
public class RaceManagerSingleton {
    private static RaceManager instance;
    
    /**
     * Get the singleton instance of RaceManager
     * 
     * @return The RaceManager instance
     */
    public static synchronized RaceManager getInstance() {
        return instance;
    }
    
    /**
     * Set the singleton instance of RaceManager
     * This should be called once during application startup
     * 
     * @param raceManager The RaceManager instance to use
     */
    public static void setInstance(RaceManager raceManager) {
        instance = raceManager;
    }
}