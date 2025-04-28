import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.lang.Math;

/**
 * A race with multiple horses, each running in its own lane
 * for a given distance. This version is adapted to work with
 * the new GUI-based racing system while maintaining backward
 * compatibility with console-based racing.
 * 
 * @author McRaceface (with significant enhancements)
 * @version 2.1
 */
public class Race
{
    private int raceLength;
    private List<Horse> horses;
    private List<Integer> laneAssignments;
    private Horse winner; // Track the first horse to win
    private static final int MAX_RACE_ROUNDS = 500; // Maximum race rounds to prevent infinite loop
    private Track track; // Reference to the track object
    private RaceManager raceManager; // Reference to the race manager (if used with GUI)
    private boolean useConsoleOutput; // Whether to use console output for races
    private long raceStartTime;    // Track when the race started (for timing)
    private long raceEndTime;      // Track when the race ended

    /**
     * Constructor for objects of class Race
     * Initially there are no horses in the lanes
     * 
     * @param distance the length of the racetrack (in metres/yards...)
     */
    public Race(int distance)
    {
        // Initialize instance variables
        this.raceLength = distance;
        this.horses = new ArrayList<>();
        this.laneAssignments = new ArrayList<>();
        this.winner = null;
        this.useConsoleOutput = true;
        
        // Create a default track (oval)
        this.track = new OvalTrack("Default Track", distance, 5, TrackCondition.DRY);
    }
    
    /**
     * Constructor with track and race manager
     * 
     * @param track The track for the race
     * @param manager The race manager (optional, can be null)
     */
    public Race(Track track, RaceManager manager) {
        this.raceLength = track.getLength();
        this.horses = new ArrayList<>();
        this.laneAssignments = new ArrayList<>();
        this.winner = null;
        this.track = track;
        this.raceManager = manager;
        this.useConsoleOutput = (manager == null);
    }
    
    /**
     * Adds a horse to the race in a given lane
     * 
     * @param theHorse the horse to be added to the race
     * @param laneNumber the lane that the horse will be added to
     * @return true if horse was successfully added, false otherwise
     */
    public boolean addHorse(Horse theHorse, int laneNumber)
    {
        if (theHorse == null) {
            System.out.println("Cannot add null horse to race");
            return false;
        }
        
        if (laneNumber < 0 || laneNumber >= track.getLanes()) {
            System.out.println("Cannot add horse to lane " + laneNumber + " because there is no such lane");
            return false;
        }
        
        // Check if the lane is already occupied
        if (laneAssignments.contains(laneNumber)) {
            System.out.println("Cannot add horse to lane " + laneNumber + " because it is already occupied");
            return false;
        }
        
        horses.add(theHorse);
        laneAssignments.add(laneNumber);
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
            laneAssignments.remove(index);
            return true;
        }
        return false;
    }
    
    /**
     * Start the race with statistics tracking
     * The horses are brought to the start and
     * then repeatedly moved forward until the 
     * race is finished
     */
    public void startRace()
    {
        // Declare a local variable to tell us when the race is finished
        boolean finished = false;
        winner = null; // Reset winner for new race
        int raceRounds = 0; // Count race rounds
        
        // Record start time for statistics
        raceStartTime = System.currentTimeMillis();
        
        // Store initial confidence levels for statistics
        Map<Horse, Double> initialConfidence = new HashMap<>();
        for (Horse horse : horses) {
            initialConfidence.put(horse, horse.getConfidence());
            horse.goBackToStart();
        }
                      
        while (!finished && raceRounds < MAX_RACE_ROUNDS)
        {
            raceRounds++; // Increment race round counter
            
            // Move each horse
            for (int i = 0; i < horses.size(); i++) {
                Horse horse = horses.get(i);
                int lane = laneAssignments.get(i);
                
                if (!horse.hasFallen()) {
                    // Get curvature of track at horse's position
                    double curveFactor = track.getCurveFactor(horse.getDistanceTravelled());
                    
                    // Calculate fall probability based on confidence, curve, and track conditions
                    double fallProb = track.getCondition().calculateFallProbability(
                        horse.getConfidence(), curveFactor, horse.getTurnHandling());
                    
                    // Check if the horse falls
                    if (Math.random() < fallProb) {
                        horse.fall();
                    } else {
                        // Move horse based on confidence
                        if (Math.random() < horse.getConfidence()) {
                            // Update movement (trackCondition speed factor, curve factor)
                            horse.updateMovement(track.getCondition().getSpeedFactor(), curveFactor);
                            
                            // Update 2D position if using GUI
                            if (track != null) {
                                java.awt.geom.Point2D.Double position = 
                                    track.calculatePosition(horse.getDistanceTravelled(), lane);
                                horse.setPosition(position.x, position.y);
                            }
                        }
                    }
                }
            }
                        
            // Print the race positions (console version only)
            if (useConsoleOutput) {
                printRace();
            }
            
            // Check if any horse has won
            checkForWinner();
            
            // Check if all horses have fallen or we have a winner
            if (winner != null || allHorsesFallen())
            {
                finished = true;
            }
           
            // Wait for 100 milliseconds
            try{ 
                TimeUnit.MILLISECONDS.sleep(100);
            } catch(Exception e){}
        }
        
        // Record race end time
        raceEndTime = System.currentTimeMillis();
        
        // Calculate race duration
        long raceDuration = raceEndTime - raceStartTime;
        
        // If max rounds reached without a winner
        if (raceRounds >= MAX_RACE_ROUNDS) {
            System.out.println("\nRace ended due to maximum number of rounds reached!");
            determineWinnerByDistance();
        }
        
        // If all horses fallen without a winner
        if (allHorsesFallen() && winner == null) {
            determineWinnerByDistance();
        }
        
        // Announce the winner (console version only)
        if (useConsoleOutput) {
            announceWinner();
        }
        
        // Update confidence levels
        updateConfidences();
        
        // Record race statistics
        recordRaceStatistics(initialConfidence, raceDuration);
    }
    
    /**
     * Record race statistics for later analysis
     * 
     * @param initialConfidence Map of horses to their initial confidence values
     * @param raceDuration Total duration of the race in milliseconds
     */
    private void recordRaceStatistics(Map<Horse, Double> initialConfidence, long raceDuration) {
        // Create race statistics object
        RaceStatistics raceStats = new RaceStatistics(
            track.getName(),
            track.getCondition(),
            track.getLength()
        );
        
        // Set race duration and winner
        raceStats.setRaceDuration(raceDuration);
        raceStats.setWinner(winner);
        
        // Record performance for each horse
        for (int i = 0; i < horses.size(); i++) {
            Horse horse = horses.get(i);
            
            // Calculate finish time for horses that completed the race
            long finishTime = 0;
            if (horse == winner || (horse.getDistanceTravelled() >= track.getLength() && !horse.hasFallen())) {
                // For simplicity, we'll use a ratio of race duration based on distance
                double completion = Math.min(1.0, horse.getDistanceTravelled() / track.getLength());
                finishTime = (long)(raceDuration * completion);
            }
            
            // Add performance data
            raceStats.addHorsePerformance(
                horse,
                finishTime,
                horse.getDistanceTravelled(),
                horse.hasFallen(),
                initialConfidence.getOrDefault(horse, 0.0),
                horse.getConfidence()
            );
        }
        
        // Add race statistics to the statistics manager
        StatisticsManager.getInstance().recordRace(raceStats);
    }
    
    /**
     * Check if all horses have fallen
     * 
     * @return true if all horses have fallen, false otherwise
     */
    private boolean allHorsesFallen() {
        if (horses.isEmpty()) {
            return false;
        }
        
        for (Horse horse : horses) {
            if (!horse.hasFallen()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Determine winner by distance traveled
     * Used when race ends prematurely (all horses fallen or max rounds reached)
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
    }
    
    /**
     * Check if any horse has won the race and record the first winner
     */
    private void checkForWinner()
    {
        // Only set winner if we don't already have one (first to finish line)
        if (winner == null) {
            for (Horse horse : horses) {
                if (horse != null && raceWonBy(horse)) {
                    winner = horse;
                    break;
                }
            }
        }
    }
    
    /**
     * Determines if a horse has won the race
     *
     * @param theHorse The horse we are testing
     * @return true if the horse has won, false otherwise.
     */
    private boolean raceWonBy(Horse theHorse)
    {
        if (theHorse.getDistanceTravelled() >= raceLength)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Announce the winner of the race
     */
    private void announceWinner()
    {
        System.out.println("\n*** RACE RESULTS ***");
        
        // Display winner with emphasis
        if (winner != null) {
            System.out.println("🏆 " + winner.getName() + " IS THE WINNER! 🏆");
            
            // Add explanation if race ended prematurely
            if (allHorsesFallen()) {
                System.out.println("Race ended early as all horses fell. Winner determined by distance traveled.");
            }
        } else {
            System.out.println("No horses completed the race!");
        }
        
        // Print race statistics
        System.out.println("\nRace Statistics:");
        for (int i = 0; i < horses.size(); i++) {
            Horse horse = horses.get(i);
            int lane = laneAssignments.get(i);
            printHorseStats(horse, lane);
        }
        
        // Print fallen horses
        System.out.println("\nFallen horses:");
        boolean anyFallen = false;
        for (Horse horse : horses) {
            if (horse != null && horse.hasFallen()) {
                System.out.println(horse.getName() + " has fallen at position " + horse.getDistanceTravelled());
                anyFallen = true;
            }
        }
        if (!anyFallen) {
            System.out.println("No horses fell during this race!");
        }
        
        // Print final confidence values
        System.out.println("\nHorse confidence values:");
        for (Horse horse : horses) {
            if (horse != null) {
                System.out.printf("%s (Current confidence %.2f)%n", horse.getName(), horse.getConfidence());
            }
        }
    }
    
    /**
     * Print statistics for a single horse
     */
    private void printHorseStats(Horse horse, int lane) {
        if (horse != null) {
            System.out.printf("Lane %d: %s - Distance: %.1f/%d%s%n", 
                lane, 
                horse.getName(), 
                horse.getDistanceTravelled(), 
                raceLength,
                horse == winner ? " (WINNER)" : "");
        }
    }
    
    /**
     * Update confidence levels for horses based on race results:
     * - Winners get a confidence boost
     * - Fallen horses get a confidence reduction
     * - Non-winners who didn't fall get a small adjustment based on performance
     */
    private void updateConfidences()
    {
        // Adjust confidence for winner (increase by 10%)
        if (winner != null) {
            winner.setConfidence(winner.getConfidence() * 1.10);
        }
        
        // Adjust confidence for fallen horses (decrease by 5%)
        for (Horse horse : horses) {
            if (horse != null && horse.hasFallen()) {
                horse.setConfidence(horse.getConfidence() * 0.95);
            }
        }
        
        // Small adjustment for horses that completed but didn't win
        for (Horse horse : horses) {
            adjustNonWinnerConfidence(horse);
        }
    }
    
    /**
     * Apply small confidence adjustment for non-winners based on performance
     */
    private void adjustNonWinnerConfidence(Horse horse) {
        if (horse != null && horse != winner && !horse.hasFallen()) {
            // Base confidence adjustment on progress through the race
            double progress = horse.getDistanceTravelled() / raceLength;
            // Small boost for horses that got close to finishing
            if (progress > 0.8) {
                horse.setConfidence(horse.getConfidence() * 1.02);
            }
        }
    }
    
    /**
     * Clear the terminal screen
     * Uses a more portable approach than \u000C
     */
    private void clearScreen() {
        // First try ANSI escape code (works on most terminals)
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        // Fallback to printing newlines
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
    
    /***
     * Print the race on the terminal
     */
    private void printRace()
    {
        clearScreen();  // Clear the terminal window using improved method
        
        multiplePrint('=', raceLength + 3); //top edge of track
        System.out.println();
        
        for (int i = 0; i < horses.size(); i++) {
            Horse horse = horses.get(i);
            int laneNumber = laneAssignments.get(i);
            
            if (horse != null) {
                printLane(horse, laneNumber);
                System.out.println(" " + horse.getName());
            }
        }
        
        multiplePrint('=', raceLength + 3); //bottom edge of track
        System.out.println();    
    }
    
    /**
     * print a horse's lane during the race
     * for example
     * |           X                      |
     * to show how far the horse has run
     */
    private void printLane(Horse theHorse, int laneNumber)
    {
        // Calculate how many spaces are needed before
        // and after the horse
        int spacesBefore = theHorse.getDistanceAsInt();
        int spacesAfter = raceLength - theHorse.getDistanceAsInt();
        
        // Ensure we don't get negative spaces if horse goes beyond finish line
        if (spacesAfter < 0) spacesAfter = 0;
        
        // Print a | for the beginning of the lane
        System.out.print(laneNumber + ":|");
        
        // Print the spaces before the horse
        multiplePrint(' ', spacesBefore);
        
        // If the horse has fallen then print dead
        // else print the horse's symbol
        if (theHorse.hasFallen())
        {
            System.out.print('X');  // Simple ASCII for fallen horse
        }
        else
        {
            // If Unicode is likely to cause issues, use ASCII fallback
            try {
                System.out.print(theHorse.getSymbol());
            } catch (Exception e) {
                System.out.print('>'); // ASCII fallback
            }
        }
        
        // Print the spaces after the horse
        multiplePrint(' ', spacesAfter);
        
        // Print the | for the end of the track
        System.out.print('|');
    }
    
    /***
     * print a character a given number of times.
     * e.g. printmany('x',5) will print: xxxxx
     * 
     * @param aChar the character to Print
     * @param times number of times to print the character
     */
    private void multiplePrint(char aChar, int times)
    {
        // Don't try to print negative number of times
        if (times < 0) times = 0;
        
        for (int i = 0; i < times; i++) {
            System.out.print(aChar);
        }
    }
    
    /**
     * Get the current winner of the race
     * 
     * @return The winning horse, or null if there's no winner yet
     */
    public Horse getWinner() {
        return winner;
    }
    
    /**
     * Get the length of the race
     * 
     * @return The race length
     */
    public int getRaceLength() {
        return raceLength;
    }
    
    /**
     * Set the track for the race
     * 
     * @param newTrack The new track
     */
    public void setTrack(Track newTrack) {
        this.track = newTrack;
        this.raceLength = newTrack.getLength();
    }
    
    /**
     * Get the track for the race
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
     * Get the lane assignments for the horses
     * 
     * @return The list of lane assignments
     */
    public List<Integer> getLaneAssignments() {
        return new ArrayList<>(laneAssignments);
    }
    

    public static void main(String[] args) {
        // First ensure console can display Unicode
        try {
            // Force UTF-8 encoding for console output
            System.setProperty("file.encoding", "UTF-8");
            java.lang.reflect.Field charset = java.nio.charset.Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (Exception e) {
            System.out.println("Warning: Could not set UTF-8 encoding. Unicode symbols may not display correctly.");
        }
        
        // Give the user a choice between console race and GUI race
        System.out.println("Welcome to the Horse Racing Simulator!");
        System.out.println("1. Run Console Race");
        System.out.println("2. Launch GUI Race");
        System.out.print("Enter your choice (1-2): ");
        
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        int choice = 1;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to Console Race.");
            choice = 1;
        }
        
        if (choice == 2) {
            // Launch the GUI
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new RacingGUI();
                }
            });
        } else {
            // Run a console-based race
            runConsoleRace();
        }
    }
    
    /**
     * Run a console-based race for backward compatibility
     */
    private static void runConsoleRace() {
        // Create the race (50 units long)
        Race horseRace = new Race(50);
        
        // Create horses with simple ASCII symbols for compatibility
        Horse horse1 = new Horse('A', "Black Knight", 0.7);
        Horse horse2 = new Horse('B', "White Knight", 0.5);
        Horse horse3 = new Horse('C', "Lightning", 0.9);
        
        // Add horses to race lanes
        horseRace.addHorse(horse1, 0);
        horseRace.addHorse(horse2, 1);
        horseRace.addHorse(horse3, 2);
        
        // Start the race
        horseRace.startRace();
    }
}