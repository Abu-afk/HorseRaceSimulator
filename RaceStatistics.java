import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * RaceStatistics class stores and manages statistics for a single race.
 * It captures data about each horse's performance in the race.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class RaceStatistics {
    private Date raceDate;                      // When the race occurred
    private String trackName;                   // Name of the track
    private TrackCondition trackCondition;      // Track condition during the race
    private int trackLength;                    // Length of the track
    private long raceDuration;                  // Total duration of the race in milliseconds
    private Map<Horse, HorsePerformance> performances; // Performance of each horse in this race
    private Horse winner;                        // The winning horse
    
    /**
     * Constructor for RaceStatistics
     * 
     * @param trackName The name of the track
     * @param trackCondition The condition of the track
     * @param trackLength The length of the track
     */
    public RaceStatistics(String trackName, TrackCondition trackCondition, int trackLength) {
        this.raceDate = new Date(); // Current date/time
        this.trackName = trackName;
        this.trackCondition = trackCondition;
        this.trackLength = trackLength;
        this.performances = new HashMap<>();
    }
    
    /**
     * Set the race duration
     * 
     * @param duration Duration in milliseconds
     */
    public void setRaceDuration(long duration) {
        this.raceDuration = duration;
    }
    
    /**
     * Get the race duration
     * 
     * @return Duration in milliseconds
     */
    public long getRaceDuration() {
        return raceDuration;
    }
    
    /**
     * Set the winning horse
     * 
     * @param horse The winning horse
     */
    public void setWinner(Horse horse) {
        this.winner = horse;
    }
    
    /**
     * Get the winning horse
     * 
     * @return The winning horse
     */
    public Horse getWinner() {
        return winner;
    }
    
    /**
     * Get the race date
     * 
     * @return The date when the race occurred
     */
    public Date getRaceDate() {
        return raceDate;
    }
    
    /**
     * Get the track name
     * 
     * @return The name of the track
     */
    public String getTrackName() {
        return trackName;
    }
    
    /**
     * Get the track condition
     * 
     * @return The condition of the track
     */
    public TrackCondition getTrackCondition() {
        return trackCondition;
    }
    
    /**
     * Get the track length
     * 
     * @return The length of the track
     */
    public int getTrackLength() {
        return trackLength;
    }
    
    /**
     * Add or update a horse's performance in this race
     * 
     * @param horse The horse
     * @param finishTime The time to finish (0 if not finished)
     * @param distance The distance traveled
     * @param fallen Whether the horse fell
     * @param confidenceBefore The horse's confidence before the race
     * @param confidenceAfter The horse's confidence after the race
     */
    public void addHorsePerformance(Horse horse, long finishTime, double distance, 
                                   boolean fallen, double confidenceBefore, double confidenceAfter) {
        HorsePerformance performance = new HorsePerformance(
            horse, finishTime, distance, fallen, confidenceBefore, confidenceAfter);
        performances.put(horse, performance);
    }
    
    /**
     * Get a horse's performance in this race
     * 
     * @param horse The horse to look up
     * @return The performance data, or null if not found
     */
    public HorsePerformance getHorsePerformance(Horse horse) {
        return performances.get(horse);
    }
    
    /**
     * Get all horse performances in this race
     * 
     * @return List of horse performances
     */
    public List<HorsePerformance> getAllPerformances() {
        return new ArrayList<>(performances.values());
    }
    
    /**
     * Get the finishing position of a horse in this race
     * 
     * @param horse The horse to look up
     * @return The finishing position (1-based), or 0 if DNF
     */
    public int getFinishingPosition(Horse horse) {
        HorsePerformance targetPerformance = performances.get(horse);
        if (targetPerformance == null || targetPerformance.getFinishTime() == 0) {
            return 0; // DNF (Did Not Finish)
        }
        
        int position = 1;
        for (HorsePerformance performance : performances.values()) {
            // If another horse finished with a better time, increment position
            if (performance != targetPerformance && 
                performance.getFinishTime() > 0 && 
                performance.getFinishTime() < targetPerformance.getFinishTime()) {
                position++;
            }
        }
        
        return position;
    }
    
    /**
     * Calculates the best (shortest) finishing time among all horses
     * 
     * @return The best finishing time in milliseconds, or 0 if no horse finished
     */
    public long getBestFinishingTime() {
        long bestTime = Long.MAX_VALUE;
        boolean anyFinished = false;
        
        for (HorsePerformance performance : performances.values()) {
            if (performance.getFinishTime() > 0) {
                anyFinished = true;
                bestTime = Math.min(bestTime, performance.getFinishTime());
            }
        }
        
        return anyFinished ? bestTime : 0;
    }
    
    /**
     * Get the average speed of all horses that finished the race
     * 
     * @return The average speed in distance units per second
     */
    public double getAverageSpeed() {
        double totalSpeed = 0;
        int count = 0;
        
        for (HorsePerformance performance : performances.values()) {
            if (performance.getFinishTime() > 0) {
                totalSpeed += performance.getAverageSpeed();
                count++;
            }
        }
        
        return count > 0 ? totalSpeed / count : 0;
    }
    
    /**
     * Create a detailed summary of the race results
     * 
     * @return A string containing race details and all horse performances
     */
    public String createRaceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== RACE SUMMARY ===\n");
        summary.append("Date: ").append(raceDate).append("\n");
        summary.append("Track: ").append(trackName).append("\n");
        summary.append("Condition: ").append(trackCondition.getName()).append("\n");
        summary.append("Length: ").append(trackLength).append(" units\n");
        summary.append("Duration: ").append(formatTime(raceDuration)).append("\n");
        
        if (winner != null) {
            summary.append("Winner: ").append(winner.getName()).append("\n");
        } else {
            summary.append("No winner (all horses failed to finish)\n");
        }
        
        summary.append("\nHorse Performances:\n");
        
        // Sort performances by finishing position
        List<HorsePerformance> sortedPerformances = new ArrayList<>(performances.values());
        sortedPerformances.sort((p1, p2) -> {
            // Horses that finished come first
            if (p1.getFinishTime() > 0 && p2.getFinishTime() == 0) return -1;
            if (p1.getFinishTime() == 0 && p2.getFinishTime() > 0) return 1;
            // Then sort by finish time
            return Long.compare(p1.getFinishTime(), p2.getFinishTime());
        });
        
        for (HorsePerformance performance : sortedPerformances) {
            summary.append(performance.toString()).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Format time in milliseconds to mm:ss.SSS format
     * 
     * @param timeMs Time in milliseconds
     * @return Formatted time string
     */
    public static String formatTime(long timeMs) {
        if (timeMs == 0) return "DNF";
        
        long minutes = (timeMs / 60000) % 60;
        long seconds = (timeMs / 1000) % 60;
        long millis = timeMs % 1000;
        
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }
    
    /**
     * Inner class to store a horse's performance in a race
     */
    public class HorsePerformance {
        private Horse horse;             // The horse
        private long finishTime;         // Time to finish in milliseconds (0 if DNF)
        private double distanceTravelled; // Distance traveled
        private boolean fallen;          // Whether the horse fell
        private double confidenceBefore; // Confidence before race
        private double confidenceAfter;  // Confidence after race
        
        /**
         * Constructor for HorsePerformance
         */
        public HorsePerformance(Horse horse, long finishTime, double distance, 
                               boolean fallen, double confidenceBefore, double confidenceAfter) {
            this.horse = horse;
            this.finishTime = finishTime;
            this.distanceTravelled = distance;
            this.fallen = fallen;
            this.confidenceBefore = confidenceBefore;
            this.confidenceAfter = confidenceAfter;
        }
        
        /**
         * Get the horse
         * 
         * @return The horse
         */
        public Horse getHorse() {
            return horse;
        }
        
        /**
         * Get the finish time
         * 
         * @return The finish time in milliseconds, or 0 if DNF
         */
        public long getFinishTime() {
            return finishTime;
        }
        
        /**
         * Get the distance traveled
         * 
         * @return The distance traveled
         */
        public double getDistanceTravelled() {
            return distanceTravelled;
        }
        
        /**
         * Check if the horse fell
         * 
         * @return true if the horse fell, false otherwise
         */
        public boolean hasFallen() {
            return fallen;
        }
        
        /**
         * Get the confidence before the race
         * 
         * @return The confidence before
         */
        public double getConfidenceBefore() {
            return confidenceBefore;
        }
        
        /**
         * Get the confidence after the race
         * 
         * @return The confidence after
         */
        public double getConfidenceAfter() {
            return confidenceAfter;
        }
        
        /**
         * Get the confidence change
         * 
         * @return The difference in confidence (after - before)
         */
        public double getConfidenceChange() {
            return confidenceAfter - confidenceBefore;
        }
        
        /**
         * Get the finishing position
         * 
         * @return The finishing position (1-based), or 0 if DNF
         */
        public int getFinishingPosition() {
            return RaceStatistics.this.getFinishingPosition(horse);
        }
        
        /**
         * Calculate the average speed
         * 
         * @return The average speed in distance units per second, or 0 if DNF
         */
        public double getAverageSpeed() {
            if (finishTime > 0) {
                return (distanceTravelled / finishTime) * 1000; // Convert to per second
            }
            return 0;
        }
        
        /**
         * Get the completion percentage
         * 
         * @return The percentage of the track completed
         */
        public double getCompletionPercentage() {
            return (distanceTravelled / trackLength) * 100;
        }
        
        /**
         * String representation of the performance
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(horse.getName()).append(" (").append(horse.getBreed().getName()).append("): ");
            
            if (finishTime > 0) {
                sb.append("Position: ").append(getFinishingPosition());
                sb.append(", Time: ").append(formatTime(finishTime));
                sb.append(", Speed: ").append(String.format("%.2f", getAverageSpeed())).append(" units/sec");
            } else {
                sb.append("DNF");
                if (fallen) {
                    sb.append(" (Fallen)");
                }
                sb.append(", Distance: ").append(String.format("%.1f", distanceTravelled));
                sb.append(" (").append(String.format("%.1f", getCompletionPercentage())).append("%)");
            }
            
            sb.append(", Confidence: ").append(String.format("%.2f → %.2f", confidenceBefore, confidenceAfter));
            
            return sb.toString();
        }
    }
}