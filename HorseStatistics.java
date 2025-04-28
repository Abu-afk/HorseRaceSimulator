import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;

/**
 * HorseStatistics class maintains performance statistics for a single horse
 * across all races it has participated in.
 * 
 * @author (Your Name)
 * @version (1.0)
 */
public class HorseStatistics {
    private Horse horse;                  // The horse these statistics are for
    private List<RaceResult> raceHistory; // History of all races this horse has participated in
    
    /**
     * Constructor for HorseStatistics
     * 
     * @param horse The horse to track statistics for
     */
    public HorseStatistics(Horse horse) {
        this.horse = horse;
        this.raceHistory = new ArrayList<>();
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
     * Add a race result to the horse's history
     * 
     * @param raceStatistics The race statistics
     */
    public void addRaceResult(RaceStatistics raceStatistics) {
        RaceStatistics.HorsePerformance performance = raceStatistics.getHorsePerformance(horse);
        if (performance != null) {
            RaceResult result = new RaceResult(
                raceStatistics.getRaceDate(),
                raceStatistics.getTrackName(),
                raceStatistics.getTrackCondition(),
                raceStatistics.getTrackLength(),
                performance.getFinishingPosition(),
                performance.getFinishTime(),
                performance.getDistanceTravelled(),
                performance.hasFallen(),
                performance.getConfidenceBefore(),
                performance.getConfidenceAfter(),
                performance.getAverageSpeed()
            );
            
            raceHistory.add(result);
        }
    }
    
    /**
     * Get the total number of races the horse has participated in
     * 
     * @return The number of races
     */
    public int getTotalRaces() {
        return raceHistory.size();
    }
    
    /**
     * Get the number of races won by the horse
     * 
     * @return The number of wins
     */
    public int getWins() {
        int wins = 0;
        for (RaceResult result : raceHistory) {
            if (result.getPosition() == 1) {
                wins++;
            }
        }
        return wins;
    }
    
    /**
     * Get the win ratio of the horse
     * 
     * @return The win ratio (0.0 to 1.0), or 0 if no races
     */
    public double getWinRatio() {
        if (raceHistory.isEmpty()) {
            return 0.0;
        }
        return (double) getWins() / getTotalRaces();
    }
    
    /**
     * Get the number of falls
     * 
     * @return The number of races where the horse fell
     */
    public int getFalls() {
        int falls = 0;
        for (RaceResult result : raceHistory) {
            if (result.hasFallen()) {
                falls++;
            }
        }
        return falls;
    }
    
    /**
     * Get the fall rate
     * 
     * @return The fall rate (0.0 to 1.0), or 0 if no races
     */
    public double getFallRate() {
        if (raceHistory.isEmpty()) {
            return 0.0;
        }
        return (double) getFalls() / getTotalRaces();
    }
    
    /**
     * Get the number of completed races (finished without falling)
     * 
     * @return The number of completed races
     */
    public int getCompletedRaces() {
        int completed = 0;
        for (RaceResult result : raceHistory) {
            if (result.getFinishTime() > 0) {
                completed++;
            }
        }
        return completed;
    }
    
    /**
     * Get the completion rate
     * 
     * @return The completion rate (0.0 to 1.0), or 0 if no races
     */
    public double getCompletionRate() {
        if (raceHistory.isEmpty()) {
            return 0.0;
        }
        return (double) getCompletedRaces() / getTotalRaces();
    }
    
    /**
     * Get the average finishing position
     * 
     * @return The average position, or 0 if no completed races
     */
    public double getAveragePosition() {
        int sum = 0;
        int count = 0;
        
        for (RaceResult result : raceHistory) {
            if (result.getPosition() > 0) {
                sum += result.getPosition();
                count++;
            }
        }
        
        return count > 0 ? (double) sum / count : 0;
    }
    
    /**
     * Get the average speed across all completed races
     * 
     * @return The average speed, or 0 if no completed races
     */
    public double getAverageSpeed() {
        double sum = 0;
        int count = 0;
        
        for (RaceResult result : raceHistory) {
            if (result.getFinishTime() > 0) {
                sum += result.getAverageSpeed();
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    
    /**
     * Get the best finishing time on any track
     * 
     * @return The best time in milliseconds, or 0 if no completed races
     */
    public long getBestTime() {
        long bestTime = Long.MAX_VALUE;
        boolean anyFinished = false;
        
        for (RaceResult result : raceHistory) {
            if (result.getFinishTime() > 0) {
                anyFinished = true;
                bestTime = Math.min(bestTime, result.getFinishTime());
            }
        }
        
        return anyFinished ? bestTime : 0;
    }
    
    /**
     * Get the best finishing time on a specific track
     * 
     * @param trackName The name of the track
     * @return The best time in milliseconds, or 0 if no completed races on that track
     */
    public long getBestTimeOnTrack(String trackName) {
        long bestTime = Long.MAX_VALUE;
        boolean anyFinished = false;
        
        for (RaceResult result : raceHistory) {
            if (result.getTrackName().equals(trackName) && result.getFinishTime() > 0) {
                anyFinished = true;
                bestTime = Math.min(bestTime, result.getFinishTime());
            }
        }
        
        return anyFinished ? bestTime : 0;
    }
    
    /**
     * Get the average finishing time
     * 
     * @return The average time in milliseconds, or 0 if no completed races
     */
    public long getAverageTime() {
        long sum = 0;
        int count = 0;
        
        for (RaceResult result : raceHistory) {
            if (result.getFinishTime() > 0) {
                sum += result.getFinishTime();
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    
    /**
     * Get the race history
     * 
     * @return The list of race results
     */
    public List<RaceResult> getRaceHistory() {
        return Collections.unmodifiableList(raceHistory);
    }
    
    /**
     * Check if the horse's performance is improving
     * 
     * @param recentRaces The number of recent races to consider (e.g., last 3 races)
     * @return true if average speed is higher in more recent races, false otherwise
     */
    public boolean isPerformanceImproving(int recentRaces) {
        if (raceHistory.size() < recentRaces * 2) {
            return false; // Not enough races to determine a trend
        }
        
        // Calculate average speed for older races
        double olderAvgSpeed = 0;
        int olderCount = 0;
        for (int i = 0; i < raceHistory.size() - recentRaces; i++) {
            RaceResult result = raceHistory.get(i);
            if (result.getFinishTime() > 0) {
                olderAvgSpeed += result.getAverageSpeed();
                olderCount++;
            }
        }
        olderAvgSpeed = olderCount > 0 ? olderAvgSpeed / olderCount : 0;
        
        // Calculate average speed for recent races
        double recentAvgSpeed = 0;
        int recentCount = 0;
        for (int i = raceHistory.size() - recentRaces; i < raceHistory.size(); i++) {
            RaceResult result = raceHistory.get(i);
            if (result.getFinishTime() > 0) {
                recentAvgSpeed += result.getAverageSpeed();
                recentCount++;
            }
        }
        recentAvgSpeed = recentCount > 0 ? recentAvgSpeed / recentCount : 0;
        
        // Compare recent to older performance
        return recentAvgSpeed > olderAvgSpeed;
    }
    
    /**
     * Get a textual summary of the horse's statistics
     * 
     * @return A string containing statistics summary
     */
    public String getStatisticsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== STATISTICS FOR ").append(horse.getName()).append(" ===\n");
        summary.append("Breed: ").append(horse.getBreed().getName()).append("\n");
        summary.append("Current Confidence: ").append(String.format("%.2f", horse.getConfidence())).append("\n\n");
        
        summary.append("RACE RECORD:\n");
        summary.append("Total Races: ").append(getTotalRaces()).append("\n");
        summary.append("Wins: ").append(getWins()).append(" (")
               .append(String.format("%.1f%%", getWinRatio() * 100)).append(")\n");
        summary.append("Completed Races: ").append(getCompletedRaces()).append(" (")
               .append(String.format("%.1f%%", getCompletionRate() * 100)).append(")\n");
        summary.append("Falls: ").append(getFalls()).append(" (")
               .append(String.format("%.1f%%", getFallRate() * 100)).append(")\n");
        summary.append("Average Position: ").append(String.format("%.1f", getAveragePosition())).append("\n\n");
        
        summary.append("PERFORMANCE METRICS:\n");
        summary.append("Average Speed: ").append(String.format("%.2f", getAverageSpeed())).append(" units/sec\n");
        
        long bestTime = getBestTime();
        if (bestTime > 0) {
            summary.append("Best Time: ").append(RaceStatistics.formatTime(bestTime)).append("\n");
        } else {
            summary.append("Best Time: N/A\n");
        }
        
        long avgTime = getAverageTime();
        if (avgTime > 0) {
            summary.append("Average Time: ").append(RaceStatistics.formatTime(avgTime)).append("\n");
        } else {
            summary.append("Average Time: N/A\n");
        }
        
        summary.append("\nRECENT PERFORMANCE: ");
        if (isPerformanceImproving(3)) {
            summary.append("IMPROVING\n");
        } else {
            summary.append("STABLE/DECLINING\n");
        }
        
        summary.append("\nRACE HISTORY:\n");
        int raceNum = 1;
        for (RaceResult result : raceHistory) {
            summary.append(raceNum++).append(". ").append(result.toString()).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Inner class representing a single race result for the horse
     */
    public static class RaceResult {
        private Date raceDate;
        private String trackName;
        private TrackCondition trackCondition;
        private int trackLength;
        private int position;
        private long finishTime;
        private double distanceTravelled;
        private boolean fallen;
        private double confidenceBefore;
        private double confidenceAfter;
        private double averageSpeed;
        
        /**
         * Constructor for RaceResult
         */
        public RaceResult(Date raceDate, String trackName, TrackCondition trackCondition, int trackLength,
                         int position, long finishTime, double distance, boolean fallen,
                         double confidenceBefore, double confidenceAfter, double averageSpeed) {
            this.raceDate = raceDate;
            this.trackName = trackName;
            this.trackCondition = trackCondition;
            this.trackLength = trackLength;
            this.position = position;
            this.finishTime = finishTime;
            this.distanceTravelled = distance;
            this.fallen = fallen;
            this.confidenceBefore = confidenceBefore;
            this.confidenceAfter = confidenceAfter;
            this.averageSpeed = averageSpeed;
        }
        
        // Getter methods
        public Date getRaceDate() { return raceDate; }
        public String getTrackName() { return trackName; }
        public TrackCondition getTrackCondition() { return trackCondition; }
        public int getTrackLength() { return trackLength; }
        public int getPosition() { return position; }
        public long getFinishTime() { return finishTime; }
        public double getDistanceTravelled() { return distanceTravelled; }
        public boolean hasFallen() { return fallen; }
        public double getConfidenceBefore() { return confidenceBefore; }
        public double getConfidenceAfter() { return confidenceAfter; }
        public double getAverageSpeed() { return averageSpeed; }
        
        /**
         * Get the confidence change
         * 
         * @return The difference in confidence (after - before)
         */
        public double getConfidenceChange() {
            return confidenceAfter - confidenceBefore;
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
         * String representation of the race result
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(raceDate).append(" - ").append(trackName).append(" (").append(trackCondition.getName()).append("): ");
            
            if (finishTime > 0) {
                sb.append("Position: ").append(position);
                sb.append(", Time: ").append(RaceStatistics.formatTime(finishTime));
                sb.append(", Speed: ").append(String.format("%.2f", averageSpeed)).append(" units/sec");
            } else {
                sb.append("DNF");
                if (fallen) {
                    sb.append(" (Fallen)");
                }
                sb.append(", Distance: ").append(String.format("%.1f", distanceTravelled));
                sb.append(" (").append(String.format("%.1f", getCompletionPercentage())).append("%)");
            }
            
            double confidenceChange = getConfidenceChange();
            String changeSymbol = confidenceChange > 0 ? "+" : "";
            sb.append(", Confidence Change: ").append(changeSymbol)
              .append(String.format("%.2f", confidenceChange));
            
            return sb.toString();
        }
    }
}