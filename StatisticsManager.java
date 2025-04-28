import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;

/**
 * StatisticsManager is a central repository for all race and horse statistics.
 * It manages a collection of race statistics and horse-specific statistics.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class StatisticsManager {
    private static StatisticsManager instance;  // Singleton instance
    
    private List<RaceStatistics> raceHistory;    // All races
    private Map<Horse, HorseStatistics> horseStats; // Statistics for each horse
    private Map<String, TrackRecord> trackRecords; // Best times for each track
    
    /**
     * Private constructor for singleton pattern
     */
    private StatisticsManager() {
        raceHistory = new ArrayList<>();
        horseStats = new HashMap<>();
        trackRecords = new HashMap<>();
    }
    
    /**
     * Get the singleton instance
     * 
     * @return The singleton instance
     */
    public static synchronized StatisticsManager getInstance() {
        if (instance == null) {
            instance = new StatisticsManager();
        }
        return instance;
    }
    
    /**
     * Record a new race and update all associated statistics
     * 
     * @param statistics The race statistics to record
     */
    public void recordRace(RaceStatistics statistics) {
        // Add to race history
        raceHistory.add(statistics);
        
        // Update track records if this race has a new best time
        long bestTime = statistics.getBestFinishingTime();
        if (bestTime > 0) {
            String trackName = statistics.getTrackName();
            TrackCondition condition = statistics.getTrackCondition();
            
            // Get or create track record
            String trackKey = trackName + "-" + condition.getName();
            TrackRecord record = trackRecords.getOrDefault(trackKey, 
                new TrackRecord(trackName, condition));
            
            // Update if this is a new best time
            if (bestTime < record.getBestTime() || record.getBestTime() == 0) {
                Horse winner = statistics.getWinner();
                record.updateRecord(bestTime, winner, new Date());
                trackRecords.put(trackKey, record);
            }
        }
        
        // Update statistics for each horse in the race
        for (RaceStatistics.HorsePerformance performance : statistics.getAllPerformances()) {
            Horse horse = performance.getHorse();
            
            // Get or create horse statistics
            HorseStatistics horseStat = horseStats.getOrDefault(horse, new HorseStatistics(horse));
            
            // Add this race result
            horseStat.addRaceResult(statistics);
            
            // Store updated statistics
            horseStats.put(horse, horseStat);
        }
    }
    
    /**
     * Get statistics for a specific horse
     * 
     * @param horse The horse to look up
     * @return The horse statistics, or null if not found
     */
    public HorseStatistics getHorseStatistics(Horse horse) {
        return horseStats.get(horse);
    }
    
    /**
     * Get all horse statistics
     * 
     * @return A list of all horse statistics
     */
    public List<HorseStatistics> getAllHorseStatistics() {
        return new ArrayList<>(horseStats.values());
    }
    
    /**
     * Get all race statistics
     * 
     * @return A list of all race statistics
     */
    public List<RaceStatistics> getAllRaceStatistics() {
        return Collections.unmodifiableList(raceHistory);
    }
    
    /**
     * Get all track records
     * 
     * @return A list of all track records
     */
    public List<TrackRecord> getAllTrackRecords() {
        return new ArrayList<>(trackRecords.values());
    }
    
    /**
     * Get the track record for a specific track and condition
     * 
     * @param trackName The name of the track
     * @param condition The track condition
     * @return The track record, or null if not found
     */
    public TrackRecord getTrackRecord(String trackName, TrackCondition condition) {
        return trackRecords.get(trackName + "-" + condition.getName());
    }
    
    /**
     * Get the number of races recorded
     * 
     * @return The number of races
     */
    public int getRaceCount() {
        return raceHistory.size();
    }
    
    /**
     * Get the number of horses with statistics
     * 
     * @return The number of horses
     */
    public int getHorseCount() {
        return horseStats.size();
    }
    
    /**
     * Get the top performing horses based on win ratio
     * 
     * @param limit The maximum number of horses to return
     * @return A list of horse statistics sorted by win ratio (highest first)
     */
    public List<HorseStatistics> getTopPerformingHorses(int limit) {
        List<HorseStatistics> topHorses = new ArrayList<>(horseStats.values());
        
        // Sort by win ratio
        topHorses.sort((h1, h2) -> Double.compare(h2.getWinRatio(), h1.getWinRatio()));
        
        // Limit the result size
        if (topHorses.size() > limit) {
            topHorses = topHorses.subList(0, limit);
        }
        
        return topHorses;
    }
    
    /**
     * Get the fastest horses based on average speed
     * 
     * @param limit The maximum number of horses to return
     * @return A list of horse statistics sorted by average speed (highest first)
     */
    public List<HorseStatistics> getFastestHorses(int limit) {
        List<HorseStatistics> fastestHorses = new ArrayList<>();
        
        // Only include horses with completed races
        for (HorseStatistics stats : horseStats.values()) {
            if (stats.getCompletedRaces() > 0) {
                fastestHorses.add(stats);
            }
        }
        
        // Sort by average speed
        fastestHorses.sort((h1, h2) -> Double.compare(h2.getAverageSpeed(), h1.getAverageSpeed()));
        
        // Limit the result size
        if (fastestHorses.size() > limit) {
            fastestHorses = fastestHorses.subList(0, limit);
        }
        
        return fastestHorses;
    }
    
    /**
     * Get horses that have improved the most recently
     * 
     * @param limit The maximum number of horses to return
     * @return A list of horse statistics for improving horses
     */
    public List<HorseStatistics> getMostImprovedHorses(int limit) {
        List<HorseStatistics> improvingHorses = new ArrayList<>();
        
        // Only include horses with enough race history
        for (HorseStatistics stats : horseStats.values()) {
            if (stats.getRaceHistory().size() >= 3 && stats.isPerformanceImproving(3)) {
                improvingHorses.add(stats);
            }
        }
        
        // Sort by total races (more races = more reliable improvement data)
        improvingHorses.sort((h1, h2) -> Integer.compare(h2.getTotalRaces(), h1.getTotalRaces()));
        
        // Limit the result size
        if (improvingHorses.size() > limit) {
            improvingHorses = improvingHorses.subList(0, limit);
        }
        
        return improvingHorses;
    }
    
    /**
     * Get a general statistics summary
     * 
     * @return A string with summary statistics
     */
    public String getStatisticsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== RACING STATISTICS SUMMARY ===\n\n");
        
        summary.append("Total Races: ").append(getRaceCount()).append("\n");
        summary.append("Horses Tracked: ").append(getHorseCount()).append("\n\n");
        
        // Track records
        summary.append("TRACK RECORDS:\n");
        List<TrackRecord> records = getAllTrackRecords();
        if (records.isEmpty()) {
            summary.append("No track records available.\n\n");
        } else {
            for (TrackRecord record : records) {
                summary.append(record.toString()).append("\n");
            }
            summary.append("\n");
        }
        
        // Top horses
        summary.append("TOP PERFORMING HORSES:\n");
        List<HorseStatistics> topHorses = getTopPerformingHorses(5);
        if (topHorses.isEmpty()) {
            summary.append("No horse statistics available.\n\n");
        } else {
            for (int i = 0; i < topHorses.size(); i++) {
                HorseStatistics stats = topHorses.get(i);
                summary.append(i + 1).append(". ")
                       .append(stats.getHorse().getName())
                       .append(" - Wins: ").append(stats.getWins())
                       .append("/").append(stats.getTotalRaces())
                       .append(" (").append(String.format("%.1f%%", stats.getWinRatio() * 100)).append(")\n");
            }
            summary.append("\n");
        }
        
        // Fastest horses
        summary.append("FASTEST HORSES:\n");
        List<HorseStatistics> fastestHorses = getFastestHorses(5);
        if (fastestHorses.isEmpty()) {
            summary.append("No speed statistics available.\n\n");
        } else {
            for (int i = 0; i < fastestHorses.size(); i++) {
                HorseStatistics stats = fastestHorses.get(i);
                summary.append(i + 1).append(". ")
                       .append(stats.getHorse().getName())
                       .append(" - Avg Speed: ").append(String.format("%.2f", stats.getAverageSpeed()))
                       .append(" units/sec\n");
            }
            summary.append("\n");
        }
        
        // Most improved horses
        summary.append("MOST IMPROVED HORSES:\n");
        List<HorseStatistics> improvedHorses = getMostImprovedHorses(3);
        if (improvedHorses.isEmpty()) {
            summary.append("No improvement statistics available.\n\n");
        } else {
            for (int i = 0; i < improvedHorses.size(); i++) {
                HorseStatistics stats = improvedHorses.get(i);
                summary.append(i + 1).append(". ")
                       .append(stats.getHorse().getName())
                       .append(" - Races: ").append(stats.getTotalRaces())
                       .append(", Recent improvement detected\n");
            }
        }
        
        return summary.toString();
    }
    
    /**
     * Clear all statistics (for testing purposes)
     */
    public void clearAllStatistics() {
        raceHistory.clear();
        horseStats.clear();
        trackRecords.clear();
    }
    
    /**
     * Inner class representing a track record (best time)
     */
    public static class TrackRecord {
        private String trackName;
        private TrackCondition trackCondition;
        private long bestTime;
        private Horse recordHolder;
        private Date recordDate;
        
        /**
         * Constructor for TrackRecord
         */
        public TrackRecord(String trackName, TrackCondition condition) {
            this.trackName = trackName;
            this.trackCondition = condition;
            this.bestTime = 0; // No record yet
            this.recordHolder = null;
            this.recordDate = null;
        }
        
        /**
         * Update the record with a new best time
         */
        public void updateRecord(long newBestTime, Horse horse, Date date) {
            this.bestTime = newBestTime;
            this.recordHolder = horse;
            this.recordDate = date;
        }
        
        // Getter methods
        public String getTrackName() { return trackName; }
        public TrackCondition getTrackCondition() { return trackCondition; }
        public long getBestTime() { return bestTime; }
        public Horse getRecordHolder() { return recordHolder; }
        public Date getRecordDate() { return recordDate; }
        
        /**
         * String representation of the track record
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(trackName).append(" (").append(trackCondition.getName()).append("): ");
            
            if (bestTime > 0 && recordHolder != null) {
                sb.append(RaceStatistics.formatTime(bestTime));
                sb.append(" - Set by ").append(recordHolder.getName());
                sb.append(" on ").append(recordDate);
            } else {
                sb.append("No record");
            }
            
            return sb.toString();
        }
    }
}