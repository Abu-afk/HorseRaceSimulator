import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * StatisticsExporter provides functionality to export racing statistics
 * to various file formats for external analysis or backup purposes.
 * 
 * This class supports:
 * - Serialized object storage for complete data backup
 * - CSV exports for spreadsheet analysis
 * - Plain text reports for readability
 * 
 * @author Your Name
 * @version 1.0
 */
public class StatisticsExporter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    
    /**
     * Export all statistics to serialized files
     * 
     * @param directory The directory to save the files to
     * @return true if successful, false otherwise
     */
    public static boolean exportSerializedData(String directory) {
        StatisticsManager manager = StatisticsManager.getInstance();
        File dir = new File(directory);
        
        // Create directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        try {
            // Export race statistics
            File raceStatsFile = new File(dir, "race_stats.ser");
            ObjectOutputStream raceOut = new ObjectOutputStream(new FileOutputStream(raceStatsFile));
            raceOut.writeObject(manager.getAllRaceStatistics());
            raceOut.close();
            
            // Export horse statistics
            File horseStatsFile = new File(dir, "horse_stats.ser");
            ObjectOutputStream horseOut = new ObjectOutputStream(new FileOutputStream(horseStatsFile));
            horseOut.writeObject(manager.getAllHorseStatistics());
            horseOut.close();
            
            // Export track records
            File trackRecordsFile = new File(dir, "track_records.ser");
            ObjectOutputStream trackOut = new ObjectOutputStream(new FileOutputStream(trackRecordsFile));
            trackOut.writeObject(manager.getAllTrackRecords());
            trackOut.close();
            
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting serialized data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Import serialized statistics data
     * 
     * @param directory The directory containing the serialized files
     * @return true if successful, false otherwise
     */
    public static boolean importSerializedData(String directory) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        
        try {
            // Import race statistics
            File raceStatsFile = new File(dir, "race_stats.ser");
            if (raceStatsFile.exists()) {
                ObjectInputStream raceIn = new ObjectInputStream(new FileInputStream(raceStatsFile));
                List<RaceStatistics> raceStats = (List<RaceStatistics>) raceIn.readObject();
                // TODO: Add method to StatisticsManager to import race statistics
                raceIn.close();
            }
            
            // Import horse statistics
            File horseStatsFile = new File(dir, "horse_stats.ser");
            if (horseStatsFile.exists()) {
                ObjectInputStream horseIn = new ObjectInputStream(new FileInputStream(horseStatsFile));
                List<HorseStatistics> horseStats = (List<HorseStatistics>) horseIn.readObject();
                // TODO: Add method to StatisticsManager to import horse statistics
                horseIn.close();
            }
            
            // Import track records
            File trackRecordsFile = new File(dir, "track_records.ser");
            if (trackRecordsFile.exists()) {
                ObjectInputStream trackIn = new ObjectInputStream(new FileInputStream(trackRecordsFile));
                List<StatisticsManager.TrackRecord> trackRecords = (List<StatisticsManager.TrackRecord>) trackIn.readObject();
                // TODO: Add method to StatisticsManager to import track records
                trackIn.close();
            }
            
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error importing serialized data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export horse statistics to CSV
     * 
     * @param filePath The file to save the CSV data to
     * @return true if successful, false otherwise
     */
    public static boolean exportHorseStatisticsToCSV(String filePath) {
        StatisticsManager manager = StatisticsManager.getInstance();
        List<HorseStatistics> horseStats = manager.getAllHorseStatistics();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write CSV header
            writer.println("Horse Name,Breed,Total Races,Wins,Win Ratio,Completed Races,Falls,Avg Position,Avg Speed,Best Time,Avg Time");
            
            // Write each horse's data
            for (HorseStatistics stats : horseStats) {
                Horse horse = stats.getHorse();
                writer.printf("%s,%s,%d,%d,%.2f,%d,%d,%.2f,%.2f,%s,%s%n",
                    horse.getName().replace(",", " "),
                    horse.getBreed().getName().replace(",", " "),
                    stats.getTotalRaces(),
                    stats.getWins(),
                    stats.getWinRatio(),
                    stats.getCompletedRaces(),
                    stats.getFalls(),
                    stats.getAveragePosition(),
                    stats.getAverageSpeed(),
                    RaceStatistics.formatTime(stats.getBestTime()),
                    RaceStatistics.formatTime(stats.getAverageTime())
                );
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting horse statistics to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export track records to CSV
     * 
     * @param filePath The file to save the CSV data to
     * @return true if successful, false otherwise
     */
    public static boolean exportTrackRecordsToCSV(String filePath) {
        StatisticsManager manager = StatisticsManager.getInstance();
        List<StatisticsManager.TrackRecord> records = manager.getAllTrackRecords();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write CSV header
            writer.println("Track Name,Condition,Best Time,Record Holder,Record Date");
            
            // Write each track record
            for (StatisticsManager.TrackRecord record : records) {
                if (record.getBestTime() > 0 && record.getRecordHolder() != null) {
                    writer.printf("%s,%s,%s,%s,%s%n",
                        record.getTrackName().replace(",", " "),
                        record.getTrackCondition().getName().replace(",", " "),
                        RaceStatistics.formatTime(record.getBestTime()),
                        record.getRecordHolder().getName().replace(",", " "),
                        record.getRecordDate() != null ? DATE_FORMAT.format(record.getRecordDate()) : "N/A"
                    );
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting track records to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Export detailed race results for a horse to CSV
     * 
     * @param horse The horse to export results for
     * @param filePath The file to save the CSV data to
     * @return true if successful, false otherwise
     */
    public static boolean exportHorseRaceHistoryToCSV(Horse horse, String filePath) {
        StatisticsManager manager = StatisticsManager.getInstance();
        HorseStatistics stats = manager.getHorseStatistics(horse);
        
        if (stats == null) {
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write CSV header
            writer.println("Race Date,Track,Condition,Position,Time,Speed,Distance,Completion %,Fallen,Confidence Before,Confidence After,Change");
            
            // Write each race result
            for (HorseStatistics.RaceResult result : stats.getRaceHistory()) {
                writer.printf("%s,%s,%s,%d,%s,%.2f,%.2f,%.2f,%s,%.2f,%.2f,%.2f%n",
                    DATE_FORMAT.format(result.getRaceDate()),
                    result.getTrackName().replace(",", " "),
                    result.getTrackCondition().getName(),
                    result.getPosition(),
                    RaceStatistics.formatTime(result.getFinishTime()),
                    result.getAverageSpeed(),
                    result.getDistanceTravelled(),
                    result.getCompletionPercentage(),
                    result.hasFallen() ? "Yes" : "No",
                    result.getConfidenceBefore(),
                    result.getConfidenceAfter(),
                    result.getConfidenceChange()
                );
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting horse race history to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate a comprehensive text report with all statistics
     * 
     * @param filePath The file to save the report to
     * @return true if successful, false otherwise
     */
    public static boolean generateComprehensiveReport(String filePath) {
        StatisticsManager manager = StatisticsManager.getInstance();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write report header
            writer.println("=================================================");
            writer.println("  HORSE RACING SIMULATOR - STATISTICS REPORT");
            writer.println("=================================================");
            writer.println("Generated: " + new java.util.Date());
            writer.println("\n");
            
            // Write summary statistics
            writer.println(manager.getStatisticsSummary());
            writer.println("\n");
            
            // Write track records
            writer.println("=================================================");
            writer.println("  TRACK RECORDS");
            writer.println("=================================================");
            List<StatisticsManager.TrackRecord> records = manager.getAllTrackRecords();
            if (records.isEmpty()) {
                writer.println("No track records available.");
            } else {
                for (StatisticsManager.TrackRecord record : records) {
                    writer.println(record.toString());
                }
            }
            writer.println("\n");
            
            // Write horse statistics
            writer.println("=================================================");
            writer.println("  HORSE STATISTICS");
            writer.println("=================================================");
            List<HorseStatistics> horseStats = manager.getAllHorseStatistics();
            if (horseStats.isEmpty()) {
                writer.println("No horse statistics available.");
            } else {
                for (HorseStatistics stats : horseStats) {
                    writer.println(stats.getStatisticsSummary());
                    writer.println("\n");
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error generating comprehensive report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}