import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.awt.geom.Point2D;

/**
 * RaceManager class manages the race simulation, horses, track, and conditions.
 * It acts as a controller between the UI and the race mechanics.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class RaceManager {
    private Track track;                // The current track
    private List<Horse> horses;         // List of horses in the race
    private List<Integer> lanes;        // Which lane each horse is in
    private boolean raceInProgress;     // Whether a race is currently running
    private Horse winner;               // The winning horse
    private Random random;              // Random number generator
    private List<RaceListener> listeners; // Listeners for race events
    private static final int MAX_RACE_ROUNDS = 1000; // Maximum race rounds to prevent infinite loops
    private Map<Horse, Double> initialConfidence; // Track initial confidence of horses
    private long raceStartTime;         // When the race started
    private long raceEndTime;           // When the race ended
    
    // Add this enum for race status
    public enum RaceStatus {
        PENDING,    // Race not yet started, accepting bets
        IN_PROGRESS, // Race is currently running
        COMPLETED   // Race has finished
    }

    // Add a field to track current race status
    private RaceStatus currentRaceStatus = RaceStatus.PENDING;

    /**
     * Constructor for the RaceManager class
     */
    public RaceManager() {
        horses = new ArrayList<>();
        lanes = new ArrayList<>();
        random = new Random();
        raceInProgress = false;
        listeners = new ArrayList<>();
        initialConfidence = new HashMap<>();
        
        // Create a default track
        track = new OvalTrack("Standard Oval", 500, 5, TrackCondition.DRY);
    }
    
    /**
     * Add a horse to the race
     * 
     * @param horse The horse to add
     * @param lane The lane to place the horse in
     * @return true if the horse was successfully added, false otherwise
     */
    public boolean addHorse(Horse horse, int lane) {
        if (horse == null) {
            System.out.println("Cannot add null horse to race");
            return false;
        }
        
        if (lane < 0 || lane >= track.getLanes()) {
            System.out.println("Invalid lane number: " + lane);
            return false;
        }
        
        // Check if the lane is already occupied
        if (lanes.contains(lane)) {
            System.out.println("Lane " + lane + " is already occupied");
            return false;
        }
        
        horses.add(horse);
        lanes.add(lane);
        return true;
    }
    
    /**
     * Remove a horse from the race
     * 
     * @param horse The horse to remove
     * @return true if the horse was successfully removed, false otherwise
     */
    public boolean removeHorse(Horse horse) {
        int index = horses.indexOf(horse);
        if (index >= 0) {
            horses.remove(index);
            lanes.remove(index);
            return true;
        }
        return false;
    }
    
    /**
     * Set the track for the race
     * 
     * @param newTrack The new track
     */
    public void setTrack(Track newTrack) {
        this.track = newTrack;
    }
    
    /**
     * Get the current track
     * 
     * @return The current track
     */
    public Track getTrack() {
        return track;
    }
    
    /**
     * Get the list of horses in the race
     * 
     * @return The list of horses
     */
    public List<Horse> getHorses() {
        return new ArrayList<>(horses);
    }
    
    /**
     * Get the lane number for a specific horse
     * 
     * @param horse The horse to look up
     * @return The lane number, or -1 if the horse is not in the race
     */
    public int getLane(Horse horse) {
        int index = horses.indexOf(horse);
        if (index >= 0) {
            return lanes.get(index);
        }
        return -1;
    }
    
    /**
     * Reset the race state to allow new bets
     */
    public void resetRaceState() {
        // Clear winner
        winner = null;
        
        // Reset race status
        currentRaceStatus = RaceStatus.PENDING;        
        // Make sure race is not marked as in progress
        raceInProgress = false;
    }
    
    /**
     * Start the race simulation
     */
    public void startRace() {
        if (horses.isEmpty()) {
            System.out.println("Cannot start race: no horses added");
            return;
        }
        
        // Reset race state
        raceInProgress = true;
        
        // Update race status
        currentRaceStatus = RaceStatus.IN_PROGRESS;
        winner = null;
        
        // Store initial confidence levels for statistics
        initialConfidence.clear();
        for (Horse horse : horses) {
            initialConfidence.put(horse, horse.getConfidence());
            horse.goBackToStart();
        }
        
        // Record start time
        raceStartTime = System.currentTimeMillis();
        
        // Notify listeners that race is starting
        for (RaceListener listener : listeners) {
            listener.onRaceStart();
        }
        
        // Start the race in a separate thread
        Thread raceThread = new Thread(this::runRace);
        raceThread.start();
    }
    
    /**
     * Run the race simulation loop
     */
    private void runRace() {
        int raceRounds = 0;
        boolean finished = false;
        
        while (raceInProgress && !finished && raceRounds < MAX_RACE_ROUNDS) {
            raceRounds++;
            
            // Update each horse's position
            for (int i = 0; i < horses.size(); i++) {
                Horse horse = horses.get(i);
                int lane = lanes.get(i);
                
                if (!horse.hasFallen()) {
                    // Get the curve factor at the horse's current position
                    double curveFactor = track.getCurveFactor(horse.getDistanceTravelled());
                    
                    // Calculate fall probability based on track condition, horse confidence, and curve
                    double fallProb = track.getCondition().calculateFallProbability(
                        horse.getConfidence(), curveFactor, horse.getTurnHandling());
                    
                    // Check if the horse falls
                    if (random.nextDouble() < fallProb) {
                        horse.fall();
                        // Notify listeners that a horse has fallen
                        for (RaceListener listener : listeners) {
                            listener.onHorseFallen(horse);
                        }
                    } else {
                        // Move the horse forward based on its speed and track conditions
                        if (random.nextDouble() < horse.getConfidence()) {
                            // Update horse movement based on track conditions and curves
                            horse.updateMovement(track.getCondition().getSpeedFactor(), curveFactor);
                            
                            // Update the horse's 2D position on the track
                            Point2D.Double position = track.calculatePosition(horse.getDistanceTravelled(), lane);
                            horse.setPosition(position.x, position.y);
                            
                            // Special handling for figure-8 tracks at the crossing
                            if (track instanceof FigureEightTrack && 
                                ((FigureEightTrack)track).isAtCrossing(horse.getDistanceTravelled())) {
                                // Slow down at the crossing
                                horse.adjustSpeed(0.7);
                            }
                        }
                    }
                    
                    // Check if the horse has finished the race
                    if (track.isRaceCompleted(horse.getDistanceTravelled()) && winner == null) {
                        winner = horse;
                        
                        // Notify listeners that a winner has been found
                        for (RaceListener listener : listeners) {
                            listener.onRaceWinner(horse);
                        }
                    }
                }
            }
            
            // Notify listeners to update UI
            for (RaceListener listener : listeners) {
                listener.onRaceUpdate();
            }
            
            // Check race end conditions
            if (winner != null || allHorsesFallen()) {
                finished = true;
            }
            
            // Short pause between updates
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Record race end time
        raceEndTime = System.currentTimeMillis();
        
        // If race was stopped due to maximum rounds being reached
        if (raceRounds >= MAX_RACE_ROUNDS) {
            System.out.println("Race ended due to maximum number of rounds reached");
            determineWinnerByDistance();
        }
        
        // If race ended with all horses fallen
        if (allHorsesFallen() && winner == null) {
            determineWinnerByDistance();
        }
        
        // Race is now over
        raceInProgress = false;
        
        currentRaceStatus = RaceStatus.COMPLETED;
        
        // Update horse confidences based on race results
        updateHorseConfidences();
        
        // Record statistics for this race
        recordRaceStatistics();
        
        // Notify listeners that race has ended
        for (RaceListener listener : listeners) {
            listener.onRaceEnd(winner);
        }
    }
    
    /**
     * Record statistics for the current race
     * This is called automatically when a race is completed
     */
    public void recordRaceStatistics() {
        // Create race statistics object
        RaceStatistics raceStats = new RaceStatistics(
            track.getName(),
            track.getCondition(),
            track.getLength()
        );
        
        // Set race properties
        long raceDuration = raceEndTime - raceStartTime;
        raceStats.setRaceDuration(raceDuration);
        
        // Set winner
        if (winner != null) {
            raceStats.setWinner(winner);
        }
        
        // Add horse performances
        for (Horse horse : horses) {
            double distance = horse.getDistanceTravelled();
            boolean fallen = horse.hasFallen();
            
            // Calculate finish time (adjust based on your implementation)
            long finishTime = 0;
            if (horse == winner) {
                finishTime = raceDuration;
            } else if (!fallen && distance >= track.getLength()) {
                // Horse finished but wasn't the winner
                finishTime = raceDuration + 1000; // Just an example, adjust as needed
            }
            
            // Get horse's initial confidence
            double confidenceBefore = initialConfidence.getOrDefault(horse, 0.5);
            
            // Add horse performance
            raceStats.addHorsePerformance(
                horse,
                finishTime,
                distance,
                fallen,
                confidenceBefore,
                horse.getConfidence()
            );
        }
        
        // Record race statistics
        StatisticsManager.getInstance().recordRace(raceStats);
        System.out.println("Race statistics recorded for: " + track.getName());
    }
    
    /**
     * Get the finish time for a horse
     * This is a mock implementation - adjust based on your actual tracking
     */
    public long getFinishTime(Horse horse) {
        if (horse == winner) {
            return raceEndTime - raceStartTime;
        } else if (!horse.hasFallen() && horse.getDistanceTravelled() >= track.getLength()) {
            // Finished but not the winner
            return (raceEndTime - raceStartTime) + (long)(Math.random() * 5000);
        }
        return 0; // Did not finish
    }
    
    /**
     * Check if all horses have fallen
     * 
     * @return true if all horses have fallen, false otherwise
     */
    private boolean allHorsesFallen() {
        for (Horse horse : horses) {
            if (!horse.hasFallen()) {
                return false;
            }
        }
        return !horses.isEmpty();
    }
    
    /**
     * Determine a winner based on distance traveled
     * Used when the race ends prematurely (all horses fallen or maximum rounds reached)
     */
    private void determineWinnerByDistance() {
        if (horses.isEmpty()) return;
        
        Horse furthestHorse = horses.get(0);
        double maxDistance = furthestHorse.getDistanceTravelled();
        
        for (int i = 1; i < horses.size(); i++) {
            Horse horse = horses.get(i);
            if (horse.getDistanceTravelled() > maxDistance) {
                maxDistance = horse.getDistanceTravelled();
                furthestHorse = horse;
            }
        }
        
        winner = furthestHorse;
        
        // Notify listeners that a winner has been determined
        for (RaceListener listener : listeners) {
            listener.onRaceWinner(winner);
        }
    }
    
    /**
     * Update horse confidences based on race results
     */
    private void updateHorseConfidences() {
        // Boost winner's confidence
        if (winner != null) {
            winner.setConfidence(winner.getConfidence() * 1.10);
        }
        
        // Reduce confidence for fallen horses
        for (Horse horse : horses) {
            if (horse.hasFallen()) {
                horse.setConfidence(horse.getConfidence() * 0.95);
            }
        }
        
        // Adjust confidence for non-winners who didn't fall
        for (Horse horse : horses) {
            if (horse != winner && !horse.hasFallen()) {
                double progress = horse.getDistanceTravelled() / track.getLength();
                if (progress > 0.8) {
                    // Good performance but not a win
                    horse.setConfidence(horse.getConfidence() * 1.02);
                }
            }
        }
    }
    
    /**
     * Stop the current race
     */
    public void stopRace() {
        raceInProgress = false;
        currentRaceStatus = RaceStatus.COMPLETED;
    }
    
    /**
     * Check if a race is currently in progress
     * 
     * @return true if a race is in progress, false otherwise
     */
    public boolean isRaceInProgress() {
        return raceInProgress;
    }
    
    /**
     * Get the winner of the race
     * 
     * @return The winning horse, or null if there's no winner yet
     */
    public Horse getWinner() {
        return winner;
    }

    /**
     * Get the current race status
     * 
     * @return The current race status
     */
    public RaceStatus getRaceStatus() {
        return currentRaceStatus;
    }
    
    /**
     * Add a listener for race events
     * 
     * @param listener The listener to add
     */
    public void addRaceListener(RaceListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a race listener
     * 
     * @param listener The listener to remove
     */
    public void removeRaceListener(RaceListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Initialize test data for the statistics system
     */
    public void initializeTestData() {
        StatisticsManager statsManager = StatisticsManager.getInstance();
        
        // Only add test data if no races recorded yet
        if (statsManager.getRaceCount() == 0) {
            System.out.println("Initializing test race data...");
            
            // Create test horses if needed
            if (horses.isEmpty()) {
                // Create example horses for testing
                createExampleHorses();
            }
            
            // Create and record a test race
            createAndRecordTestRace();
            
            System.out.println("Test data initialization complete.");
        }
    }
    
    /**
     * Create example horses for testing
        */
    private void createExampleHorses() {
        // Thoroughbred with racing equipment
        Horse horse1 = new Horse('T', "Thunderbolt", "🏇", 0.8, 
                            HorseBreed.THOROUGHBRED, 
                            CoatColor.BAY, 
                            new HorseEquipment(
                                HorseEquipment.SaddleType.RACING,
                                HorseEquipment.HorseshoeType.LIGHTWEIGHT,
                                HorseEquipment.AccessoryType.BLINDERS));
        
        // Arabian with balanced equipment
        Horse horse2 = new Horse('A', "Silver Wind", "🐎", 0.7, 
                            HorseBreed.ARABIAN, 
                            CoatColor.GRAY, 
                            new HorseEquipment(
                                HorseEquipment.SaddleType.ENGLISH,
                                HorseEquipment.HorseshoeType.TRACTION,
                                HorseEquipment.AccessoryType.LUCKY_CHARM));
        
        // Quarter Horse with strength equipment
        Horse horse3 = new Horse('Q', "Dark Thunder", "♞", 0.85, 
                            HorseBreed.QUARTER_HORSE, 
                            CoatColor.BLACK, 
                            new HorseEquipment(
                                HorseEquipment.SaddleType.WESTERN,
                                HorseEquipment.HorseshoeType.STANDARD,
                                HorseEquipment.AccessoryType.PERFORMANCE_BRIDLE));
        
        // Add horses to the list
        if (!horses.contains(horse1)) addHorse(horse1, 0);
        if (!horses.contains(horse2)) addHorse(horse2, 1);
        if (!horses.contains(horse3)) addHorse(horse3, 2);
    }
    
    /**
     * Create and record a test race
     */
    private void createAndRecordTestRace() {
        // Create race statistics
        RaceStatistics raceStats = new RaceStatistics(
            track.getName(),
            track.getCondition(),
            track.getLength()
        );
        
        // Set race duration (between 1-3 minutes)
        long duration = 60000 + (long)(Math.random() * 120000);
        raceStats.setRaceDuration(duration);
        
        // Randomly select winner
        int winnerIndex = (int)(Math.random() * horses.size());
        Horse testWinner = horses.get(winnerIndex);
        raceStats.setWinner(testWinner);
        
        // Add performance for each horse
        for (int i = 0; i < horses.size(); i++) {
            Horse horse = horses.get(i);
            
            // Winner gets best time, others get progressively worse times
            long finishTime = (i == winnerIndex) ? 
                (duration - 10000) : (duration - 8000 + (i * 3000));
                
            // 5% chance of falling
            boolean fallen = Math.random() < 0.05;
            
            // Distance - full distance unless fallen
            double distance = fallen ? (track.getLength() * 0.7) : track.getLength();
            
            // Confidence changes
            double confidenceBefore = 0.4 + (Math.random() * 0.3);
            double confidenceAfter;
            
            if (i == winnerIndex) {
                // Winner gets confidence boost
                confidenceAfter = Math.min(1.0, confidenceBefore + 0.1);
            } else if (fallen) {
                // Fallen horses lose confidence
                confidenceAfter = Math.max(0.1, confidenceBefore - 0.15);
            } else {
                // Others get minor changes
                confidenceAfter = Math.max(0.1, Math.min(1.0, 
                    confidenceBefore + (Math.random() * 0.1 - 0.05)));
            }
            
            // Set current confidence for future races
            horse.setConfidence(confidenceAfter);
            
            // Add to race statistics
            raceStats.addHorsePerformance(
                horse,
                fallen ? 0 : finishTime,
                distance,
                fallen,
                confidenceBefore,
                confidenceAfter
            );
        }
        
        // Record the race
        StatisticsManager.getInstance().recordRace(raceStats);
        System.out.println("Test race recorded: " + track.getName());
    }
    
    /**
     * Interface for objects that want to listen for race events
     */
    public interface RaceListener {
        /**
         * Called when the race starts
         */
        void onRaceStart();
        
        /**
         * Called when a horse falls
         * 
         * @param horse The horse that fell
         */
        void onHorseFallen(Horse horse);
        
        /**
         * Called when a winner is determined
         * 
         * @param horse The winning horse
         */
        void onRaceWinner(Horse horse);
        
        /**
         * Called when the race state updates
         */
        void onRaceUpdate();
        
        /**
         * Called when the race ends
         * 
         * @param winner The winning horse (may be null if no winner)
         */
        void onRaceEnd(Horse winner);
    }
}