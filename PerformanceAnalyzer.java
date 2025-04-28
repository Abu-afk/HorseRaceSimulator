import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * PerformanceAnalyzer provides advanced statistical analysis for horse racing data.
 * It offers detailed performance metrics, trend analysis, and comparative analytics
 * that go beyond the basic statistics tracking.
 * 
 * @author Your Name
 * @version 1.0
 */
public class PerformanceAnalyzer {
    // Time period definitions for trend analysis
    public enum TimePeriod {
        RECENT_3, // Last 3 races
        RECENT_5, // Last 5 races
        RECENT_10, // Last 10 races
        ALL_TIME // All races
    }
    
    // Performance metrics for trend analysis
    public enum PerformanceMetric {
        WIN_RATIO,
        AVERAGE_POSITION,
        AVERAGE_SPEED,
        COMPLETION_RATE,
        FALL_RATE,
        CONFIDENCE
    }
    
    /**
     * Calculate performance trend for a specific horse and metric
     * 
     * @param horse The horse to analyze
     * @param metric The performance metric to track
     * @param period The time period to analyze
     * @return A TrendResult object containing trend data
     */
    public static TrendResult calculatePerformanceTrend(Horse horse, PerformanceMetric metric, TimePeriod period) {
        StatisticsManager manager = StatisticsManager.getInstance();
        HorseStatistics stats = manager.getHorseStatistics(horse);
        
        if (stats == null || stats.getTotalRaces() == 0) {
            return new TrendResult(false, 0, "No race data available for " + horse.getName());
        }
        
        List<HorseStatistics.RaceResult> history = stats.getRaceHistory();
        
        // Sort by date (newest first)
        List<HorseStatistics.RaceResult> sortedHistory = new ArrayList<>(history);
        Collections.sort(sortedHistory, (r1, r2) -> r2.getRaceDate().compareTo(r1.getRaceDate()));
        
        // Determine how many races to analyze
        int racesToAnalyze;
        switch (period) {
            case RECENT_3:
                racesToAnalyze = 3;
                break;
            case RECENT_5:
                racesToAnalyze = 5;
                break;
            case RECENT_10:
                racesToAnalyze = 10;
                break;
            case ALL_TIME:
            default:
                racesToAnalyze = sortedHistory.size();
                break;
        }
        
        // If we don't have enough races for the requested period, adjust
        if (sortedHistory.size() < racesToAnalyze) {
            racesToAnalyze = sortedHistory.size();
        }
        
        // If we have very few races, we can't calculate a meaningful trend
        if (racesToAnalyze < 2) {
            return new TrendResult(false, 0, "Not enough races to calculate a trend");
        }
        
        // Split into two periods for comparison
        int firstPeriodSize = racesToAnalyze / 2;
        int secondPeriodSize = racesToAnalyze - firstPeriodSize;
        
        List<HorseStatistics.RaceResult> recentPeriod = sortedHistory.subList(0, secondPeriodSize);
        List<HorseStatistics.RaceResult> olderPeriod = sortedHistory.subList(secondPeriodSize, racesToAnalyze);
        
        // Calculate metric for each period
        double recentValue = calculateMetric(recentPeriod, metric);
        double olderValue = calculateMetric(olderPeriod, metric);
        
        // Calculate the change
        double change = recentValue - olderValue;
        
        // Determine if the change is significant (threshold depends on metric)
        boolean isSignificant = isSignificantChange(change, metric);
        
        // Create result message
        String message;
        if (Math.abs(change) < 0.0001) {
            message = "No change in " + metricToString(metric) + " over the last " + racesToAnalyze + " races.";
        } else if (change > 0) {
            message = "Improving " + metricToString(metric) + " by " + formatChange(change, metric) + 
                      " over the last " + racesToAnalyze + " races.";
        } else {
            message = "Declining " + metricToString(metric) + " by " + formatChange(Math.abs(change), metric) + 
                      " over the last " + racesToAnalyze + " races.";
        }
        
        return new TrendResult(isSignificant, change, message);
    }
    
    /**
     * Calculate a specific performance metric for a list of race results
     */
    private static double calculateMetric(List<HorseStatistics.RaceResult> results, PerformanceMetric metric) {
        if (results.isEmpty()) {
            return 0;
        }
        
        switch (metric) {
            case WIN_RATIO:
                long wins = results.stream().filter(r -> r.getPosition() == 1).count();
                return (double) wins / results.size();
                
            case AVERAGE_POSITION:
                // Only consider completed races for position
                List<HorseStatistics.RaceResult> completed = results.stream()
                    .filter(r -> r.getPosition() > 0)
                    .collect(Collectors.toList());
                
                if (completed.isEmpty()) {
                    return 0;
                }
                
                double sumPositions = completed.stream()
                    .mapToInt(HorseStatistics.RaceResult::getPosition)
                    .sum();
                return sumPositions / completed.size();
                
            case AVERAGE_SPEED:
                // Only consider completed races for speed
                List<HorseStatistics.RaceResult> finishers = results.stream()
                    .filter(r -> r.getFinishTime() > 0)
                    .collect(Collectors.toList());
                
                if (finishers.isEmpty()) {
                    return 0;
                }
                
                double sumSpeeds = finishers.stream()
                    .mapToDouble(HorseStatistics.RaceResult::getAverageSpeed)
                    .sum();
                return sumSpeeds / finishers.size();
                
            case COMPLETION_RATE:
                long completed2 = results.stream()
                    .filter(r -> r.getFinishTime() > 0)
                    .count();
                return (double) completed2 / results.size();
                
            case FALL_RATE:
                long falls = results.stream()
                    .filter(HorseStatistics.RaceResult::hasFallen)
                    .count();
                return (double) falls / results.size();
                
            case CONFIDENCE:
                // Use the average confidence after each race
                double sumConfidence = results.stream()
                    .mapToDouble(HorseStatistics.RaceResult::getConfidenceAfter)
                    .sum();
                return sumConfidence / results.size();
                
            default:
                return 0;
        }
    }
    
    /**
     * Determine if a change in a metric is significant
     */
    private static boolean isSignificantChange(double change, PerformanceMetric metric) {
        switch (metric) {
            case WIN_RATIO:
                return Math.abs(change) >= 0.1; // 10% change in win ratio is significant
                
            case AVERAGE_POSITION:
                return Math.abs(change) >= 1.0; // 1 position difference is significant
                
            case AVERAGE_SPEED:
                return Math.abs(change) >= 0.5; // 0.5 units/sec is significant
                
            case COMPLETION_RATE:
                return Math.abs(change) >= 0.15; // 15% change in completion rate is significant
                
            case FALL_RATE:
                return Math.abs(change) >= 0.1; // 10% change in fall rate is significant
                
            case CONFIDENCE:
                return Math.abs(change) >= 0.05; // 5% change in confidence is significant
                
            default:
                return false;
        }
    }
    
    /**
     * Format a change in a metric as a string
     */
    private static String formatChange(double change, PerformanceMetric metric) {
        switch (metric) {
            case WIN_RATIO:
            case COMPLETION_RATE:
            case FALL_RATE:
            case CONFIDENCE:
                return String.format("%.1f%%", change * 100);
                
            case AVERAGE_POSITION:
                return String.format("%.1f positions", change);
                
            case AVERAGE_SPEED:
                return String.format("%.2f units/sec", change);
                
            default:
                return String.format("%.2f", change);
        }
    }
    
    /**
     * Convert a metric to a readable string
     */
    private static String metricToString(PerformanceMetric metric) {
        switch (metric) {
            case WIN_RATIO:
                return "win ratio";
            case AVERAGE_POSITION:
                return "average position";
            case AVERAGE_SPEED:
                return "average speed";
            case COMPLETION_RATE:
                return "completion rate";
            case FALL_RATE:
                return "fall rate";
            case CONFIDENCE:
                return "confidence";
            default:
                return metric.toString();
        }
    }
    
    /**
     * Compare two horses across all performance metrics
     * 
     * @param horse1 The first horse
     * @param horse2 The second horse
     * @return A ComparisonResult containing the comparison data
     */
    public static ComparisonResult compareHorses(Horse horse1, Horse horse2) {
        StatisticsManager manager = StatisticsManager.getInstance();
        HorseStatistics stats1 = manager.getHorseStatistics(horse1);
        HorseStatistics stats2 = manager.getHorseStatistics(horse2);
        
        if (stats1 == null || stats2 == null) {
            return new ComparisonResult(horse1, horse2, "One or both horses have no race data");
        }
        
        ComparisonResult result = new ComparisonResult(horse1, horse2);
        
        // Add comparisons for each metric
        result.addComparison("Total Races", stats1.getTotalRaces(), stats2.getTotalRaces());
        result.addComparison("Wins", stats1.getWins(), stats2.getWins());
        result.addComparison("Win Ratio", stats1.getWinRatio(), stats2.getWinRatio(), true);
        result.addComparison("Average Position", stats1.getAveragePosition(), stats2.getAveragePosition(), false);
        result.addComparison("Average Speed", stats1.getAverageSpeed(), stats2.getAverageSpeed(), true);
        result.addComparison("Completion Rate", stats1.getCompletionRate(), stats2.getCompletionRate(), true);
        result.addComparison("Fall Rate", stats1.getFallRate(), stats2.getFallRate(), false);
        
        // Overall comparison summary
        int horse1Advantages = 0;
        int horse2Advantages = 0;
        
        for (MetricComparison comparison : result.getComparisons()) {
            if (comparison.isHigherBetter()) {
                if (comparison.getValue1() > comparison.getValue2()) {
                    horse1Advantages++;
                } else if (comparison.getValue2() > comparison.getValue1()) {
                    horse2Advantages++;
                }
            } else {
                if (comparison.getValue1() < comparison.getValue2()) {
                    horse1Advantages++;
                } else if (comparison.getValue2() < comparison.getValue1()) {
                    horse2Advantages++;
                }
            }
        }
        
        if (horse1Advantages > horse2Advantages) {
            result.setSummary(horse1.getName() + " performs better in " + horse1Advantages + 
                             " categories compared to " + horse2Advantages + " for " + horse2.getName());
        } else if (horse2Advantages > horse1Advantages) {
            result.setSummary(horse2.getName() + " performs better in " + horse2Advantages + 
                             " categories compared to " + horse1Advantages + " for " + horse1.getName());
        } else {
            result.setSummary("Both horses perform equally well overall, with advantages in different areas");
        }
        
        return result;
    }
    
    /**
     * Find the best performing horse for a specific track and condition
     * 
     * @param trackName The name of the track
     * @param condition The track condition
     * @return The horse with the best performance, or null if no data available
     */
    public static Horse findBestHorseForTrack(String trackName, TrackCondition condition) {
        StatisticsManager manager = StatisticsManager.getInstance();
        List<HorseStatistics> allStats = manager.getAllHorseStatistics();
        
        Map<Horse, Double> trackPerformance = new HashMap<>();
        
        for (HorseStatistics stats : allStats) {
            Horse horse = stats.getHorse();
            List<HorseStatistics.RaceResult> results = stats.getRaceHistory().stream()
                .filter(r -> r.getTrackName().equals(trackName) && 
                            r.getTrackCondition().equals(condition) &&
                            r.getFinishTime() > 0)
                .collect(Collectors.toList());
            
            if (!results.isEmpty()) {
                // Calculate average performance score (lower is better)
                double avgPosition = results.stream()
                    .mapToInt(HorseStatistics.RaceResult::getPosition)
                    .average()
                    .orElse(Double.MAX_VALUE);
                
                trackPerformance.put(horse, avgPosition);
            }
        }
        
        if (trackPerformance.isEmpty()) {
            return null;
        }
        
        // Find horse with best average position (lowest value)
        return Collections.min(trackPerformance.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
    
    /**
     * Calculate a horse's consistency score (0-1)
     * Higher values indicate more consistent performance
     * 
     * @param horse The horse to analyze
     * @return A consistency score between 0 and 1
     */
    public static double calculateConsistencyScore(Horse horse) {
        StatisticsManager manager = StatisticsManager.getInstance();
        HorseStatistics stats = manager.getHorseStatistics(horse);
        
        if (stats == null || stats.getTotalRaces() < 3) {
            return 0; // Not enough data for meaningful consistency measurement
        }
        
        List<HorseStatistics.RaceResult> completedRaces = stats.getRaceHistory().stream()
            .filter(r -> r.getFinishTime() > 0)
            .collect(Collectors.toList());
        
        if (completedRaces.size() < 3) {
            return 0; // Not enough completed races
        }
        
        // Calculate position consistency
        List<Integer> positions = completedRaces.stream()
            .map(HorseStatistics.RaceResult::getPosition)
            .collect(Collectors.toList());
        
        double positionMean = positions.stream().mapToInt(Integer::intValue).average().getAsDouble();
        double positionVariance = positions.stream()
            .mapToDouble(p -> Math.pow(p - positionMean, 2))
            .sum() / positions.size();
        
        // Calculate speed consistency
        List<Double> speeds = completedRaces.stream()
            .map(HorseStatistics.RaceResult::getAverageSpeed)
            .collect(Collectors.toList());
        
        double speedMean = speeds.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        double speedVariance = speeds.stream()
            .mapToDouble(s -> Math.pow(s - speedMean, 2))
            .sum() / speeds.size();
        
        // Normalize variances (lower variance = higher consistency)
        double positionConsistency = 1.0 / (1.0 + (positionVariance / 3.0)); // Scale appropriately
        double speedConsistency = 1.0 / (1.0 + (speedVariance / speedMean));
        
        // Combine both aspects (giving more weight to position consistency)
        return (0.7 * positionConsistency + 0.3 * speedConsistency);
    }
    
    /**
     * Identify a horse's strongest and weakest track conditions
     * 
     * @param horse The horse to analyze
     * @return A TrackStrengthResult containing the analysis
     */
    public static TrackStrengthResult analyzeTrackStrengths(Horse horse) {
        StatisticsManager manager = StatisticsManager.getInstance();
        HorseStatistics stats = manager.getHorseStatistics(horse);
        
        if (stats == null || stats.getTotalRaces() == 0) {
            return new TrackStrengthResult("No race data available for " + horse.getName());
        }
        
        // Group results by track condition
        Map<TrackCondition, List<HorseStatistics.RaceResult>> resultsByCondition = new HashMap<>();
        
        for (HorseStatistics.RaceResult result : stats.getRaceHistory()) {
            TrackCondition condition = result.getTrackCondition();
            if (!resultsByCondition.containsKey(condition)) {
                resultsByCondition.put(condition, new ArrayList<>());
            }
            resultsByCondition.get(condition).add(result);
        }
        
        // Calculate performance score for each condition
        Map<TrackCondition, Double> conditionScores = new HashMap<>();
        
        for (Map.Entry<TrackCondition, List<HorseStatistics.RaceResult>> entry : resultsByCondition.entrySet()) {
            TrackCondition condition = entry.getKey();
            List<HorseStatistics.RaceResult> results = entry.getValue();
            
            // Only consider conditions with at least 2 races for meaningful comparison
            if (results.size() >= 2) {
                // Calculate a performance score (incorporating position, completion, and speed)
                double avgPosition = results.stream()
                    .filter(r -> r.getPosition() > 0)
                    .mapToInt(HorseStatistics.RaceResult::getPosition)
                    .average()
                    .orElse(0);
                
                double completionRate = (double) results.stream()
                    .filter(r -> r.getFinishTime() > 0)
                    .count() / results.size();
                
                double avgSpeed = results.stream()
                    .filter(r -> r.getFinishTime() > 0)
                    .mapToDouble(HorseStatistics.RaceResult::getAverageSpeed)
                    .average()
                    .orElse(0);
                
                // Combine factors (lower position is better)
                double positionFactor = avgPosition > 0 ? 1.0 / avgPosition : 0;
                
                // Overall score (higher is better)
                double score = (0.5 * positionFactor) + (0.3 * completionRate) + (0.2 * (avgSpeed / 10.0));
                conditionScores.put(condition, score);
            }
        }
        
        if (conditionScores.isEmpty()) {
            return new TrackStrengthResult("Not enough data to determine track strengths");
        }
        
        // Find best and worst conditions
        TrackCondition bestCondition = Collections.max(conditionScores.entrySet(), Map.Entry.comparingByValue()).getKey();
        TrackCondition worstCondition = Collections.min(conditionScores.entrySet(), Map.Entry.comparingByValue()).getKey();
        
        return new TrackStrengthResult(
            bestCondition, 
            conditionScores.get(bestCondition),
            worstCondition,
            conditionScores.get(worstCondition),
            conditionScores
        );
    }
    
    /**
     * Result class for trend analysis
     */
    public static class TrendResult {
        private boolean significant;
        private double change;
        private String message;
        
        public TrendResult(boolean significant, double change, String message) {
            this.significant = significant;
            this.change = change;
            this.message = message;
        }
        
        public boolean isSignificant() {
            return significant;
        }
        
        public double getChange() {
            return change;
        }
        
        public boolean isImproving() {
            return change > 0;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Result class for horse comparisons
     */
    public static class ComparisonResult {
        private Horse horse1;
        private Horse horse2;
        private List<MetricComparison> comparisons;
        private String summary;
        
        public ComparisonResult(Horse horse1, Horse horse2) {
            this.horse1 = horse1;
            this.horse2 = horse2;
            this.comparisons = new ArrayList<>();
        }
        
        public ComparisonResult(Horse horse1, Horse horse2, String summary) {
            this(horse1, horse2);
            this.summary = summary;
        }
        
        public void addComparison(String metricName, int value1, int value2) {
            comparisons.add(new MetricComparison(metricName, value1, value2, true));
        }
        
        public void addComparison(String metricName, double value1, double value2, boolean higherIsBetter) {
            comparisons.add(new MetricComparison(metricName, value1, value2, higherIsBetter));
        }
        
        public void setSummary(String summary) {
            this.summary = summary;
        }
        
        public Horse getHorse1() {
            return horse1;
        }
        
        public Horse getHorse2() {
            return horse2;
        }
        
        public List<MetricComparison> getComparisons() {
            return comparisons;
        }
        
        public String getSummary() {
            return summary;
        }
    }
    
    /**
     * Class representing a comparison of a single metric between two horses
     */
    public static class MetricComparison {
        private String metricName;
        private double value1;
        private double value2;
        private boolean higherIsBetter;
        
        public MetricComparison(String metricName, double value1, double value2, boolean higherIsBetter) {
            this.metricName = metricName;
            this.value1 = value1;
            this.value2 = value2;
            this.higherIsBetter = higherIsBetter;
        }
        
        public String getMetricName() {
            return metricName;
        }
        
        public double getValue1() {
            return value1;
        }
        
        public double getValue2() {
            return value2;
        }
        
        public boolean isHigherBetter() {
            return higherIsBetter;
        }
        
        public int getAdvantage() {
            if (Math.abs(value1 - value2) < 0.0001) {
                return 0; // Effectively equal
            }
            
            if (higherIsBetter) {
                return value1 > value2 ? 1 : -1;
            } else {
                return value1 < value2 ? 1 : -1;
            }
        }
    }
    
    /**
     * Result class for track strength analysis
     */
    public static class TrackStrengthResult {
        private boolean hasData;
        private String message;
        private TrackCondition bestCondition;
        private double bestScore;
        private TrackCondition worstCondition;
        private double worstScore;
        private Map<TrackCondition, Double> allScores;
        
        public TrackStrengthResult(String message) {
            this.hasData = false;
            this.message = message;
        }
        
        public TrackStrengthResult(TrackCondition bestCondition, double bestScore,
                                  TrackCondition worstCondition, double worstScore,
                                  Map<TrackCondition, Double> allScores) {
            this.hasData = true;
            this.bestCondition = bestCondition;
            this.bestScore = bestScore;
            this.worstCondition = worstCondition;
            this.worstScore = worstScore;
            this.allScores = allScores;
            
            this.message = "Best performance on " + bestCondition.getName() + 
                          " tracks, worst performance on " + worstCondition.getName() + " tracks.";
        }
        
        public boolean hasData() {
            return hasData;
        }
        
        public String getMessage() {
            return message;
        }
        
        public TrackCondition getBestCondition() {
            return bestCondition;
        }
        
        public TrackCondition getWorstCondition() {
            return worstCondition;
        }
        
        public Map<TrackCondition, Double> getAllScores() {
            return allScores;
        }
    }
}