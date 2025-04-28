import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * A JMenu component for statistics-related functionality.
 * This menu provides access to various statistics features like quick summary,
 * performance analysis, and statistics export.
 */
public class StatisticsMenu extends JMenu {
    
    private RacingGUI parentFrame;
    
    /**
     * Constructor for StatisticsMenu
     * 
     * @param parent The parent RacingGUI frame
     */
    public StatisticsMenu(RacingGUI parent) {
        super("Statistics");
        this.parentFrame = parent;
        
        // Set up menu items
        setupMenuItems();
    }
    
    /**
     * Set up the menu items
     */
    private void setupMenuItems() {
        // Quick Summary option
        JMenuItem quickSummaryItem = new JMenuItem("Quick Summary");
        quickSummaryItem.addActionListener(e -> showQuickSummary());
        add(quickSummaryItem);
        
        // Analyze Performance option
        JMenuItem analyzeItem = new JMenuItem("Analyze Performance");
        analyzeItem.addActionListener(e -> showPerformanceAnalyzer());
        add(analyzeItem);
        
        // View Statistics option
        JMenuItem viewStatsItem = new JMenuItem("View Full Statistics");
        viewStatsItem.addActionListener(e -> openStatisticsViewer());
        add(viewStatsItem);
        
        // Add separator
        addSeparator();
        
        // Export statistics
        JMenuItem exportItem = new JMenuItem("Export Statistics");
        exportItem.addActionListener(e -> exportStatistics());
        add(exportItem);
        
        // Initialize test data (for development)
        JMenuItem initTestItem = new JMenuItem("Initialize Test Data");
        initTestItem.addActionListener(e -> initializeTestData());
        add(initTestItem);
    }
    
    /**
     * Show quick summary dialog
     */
    private void showQuickSummary() {
        // Get the statistics manager
        StatisticsManager statsManager = StatisticsManager.getInstance();
        
        // Get the summary
        String summary = statsManager.getStatisticsSummary();
        
        // Check if we have any data
        if (summary.contains("Total Races: 0")) {
            JOptionPane.showMessageDialog(
                parentFrame,
                "No race statistics available yet.\nComplete a race to generate statistics.",
                "No Statistics Available",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        // Create a formatted display
        JTextArea textArea = new JTextArea(summary);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Show in a scrollable dialog
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(
            parentFrame,
            scrollPane,
            "Racing Statistics Summary",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Show performance analyzer dialog
     */
    private void showPerformanceAnalyzer() {
        // Get list of horses that have statistics
        StatisticsManager statsManager = StatisticsManager.getInstance();
        List<HorseStatistics> allStats = statsManager.getAllHorseStatistics();
        
        if (allStats.isEmpty()) {
            JOptionPane.showMessageDialog(
                parentFrame,
                "No horse statistics available yet.\nComplete a race to generate statistics.",
                "No Statistics Available",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        // Create a list of horses
        List<Horse> horses = new ArrayList<>();
        for (HorseStatistics stats : allStats) {
            horses.add(stats.getHorse());
        }
        
        // Create horse selection combo box
        JComboBox<Horse> horseCombo = new JComboBox<>();
        DefaultComboBoxModel<Horse> horseModel = new DefaultComboBoxModel<>();
        for (Horse horse : horses) {
            horseModel.addElement(horse);
        }
        horseCombo.setModel(horseModel);
        
        // Create metric selection
        JComboBox<String> metricCombo = new JComboBox<>(new String[]{
            "Win Ratio", "Average Position", "Average Speed", 
            "Completion Rate", "Fall Rate", "Confidence"
        });
        
        // Create time period selection
        JComboBox<String> periodCombo = new JComboBox<>(new String[]{
            "Last 3 Races", "Last 5 Races", "Last 10 Races", "All Races"
        });
        
        // Create panel for selection options
        JPanel selectionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        selectionPanel.add(new JLabel("Select Horse:"));
        selectionPanel.add(horseCombo);
        selectionPanel.add(new JLabel("Select Metric:"));
        selectionPanel.add(metricCombo);
        selectionPanel.add(new JLabel("Select Time Period:"));
        selectionPanel.add(periodCombo);
        
        // Show dialog for selections
        int result = JOptionPane.showConfirmDialog(
            parentFrame,
            selectionPanel,
            "Performance Analysis",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            // Get selected values
            Horse selectedHorse = (Horse) horseCombo.getSelectedItem();
            int metricIndex = metricCombo.getSelectedIndex();
            int periodIndex = periodCombo.getSelectedIndex();
            
            // Convert to enum values
            PerformanceAnalyzer.PerformanceMetric metric;
            switch (metricIndex) {
                case 0: metric = PerformanceAnalyzer.PerformanceMetric.WIN_RATIO; break;
                case 1: metric = PerformanceAnalyzer.PerformanceMetric.AVERAGE_POSITION; break;
                case 2: metric = PerformanceAnalyzer.PerformanceMetric.AVERAGE_SPEED; break;
                case 3: metric = PerformanceAnalyzer.PerformanceMetric.COMPLETION_RATE; break;
                case 4: metric = PerformanceAnalyzer.PerformanceMetric.FALL_RATE; break;
                case 5: metric = PerformanceAnalyzer.PerformanceMetric.CONFIDENCE; break;
                default: metric = PerformanceAnalyzer.PerformanceMetric.AVERAGE_SPEED;
            }
            
            PerformanceAnalyzer.TimePeriod period;
            switch (periodIndex) {
                case 0: period = PerformanceAnalyzer.TimePeriod.RECENT_3; break;
                case 1: period = PerformanceAnalyzer.TimePeriod.RECENT_5; break;
                case 2: period = PerformanceAnalyzer.TimePeriod.RECENT_10; break;
                case 3: period = PerformanceAnalyzer.TimePeriod.ALL_TIME; break;
                default: period = PerformanceAnalyzer.TimePeriod.ALL_TIME;
            }
            
            // Perform analysis
            PerformanceAnalyzer.TrendResult trendResult = 
                PerformanceAnalyzer.calculatePerformanceTrend(selectedHorse, metric, period);
            
            // Create results display
            JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
            
            // Add horse info
            JPanel horseInfoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            horseInfoPanel.setBorder(BorderFactory.createTitledBorder("Horse Info"));
            horseInfoPanel.add(new JLabel("Name:"));
            horseInfoPanel.add(new JLabel(selectedHorse.getName()));
            horseInfoPanel.add(new JLabel("Breed:"));
            horseInfoPanel.add(new JLabel(selectedHorse.getBreed().getName()));
            
            // Add trend results
            JPanel trendPanel = new JPanel(new BorderLayout());
            trendPanel.setBorder(BorderFactory.createTitledBorder("Performance Trend"));
            
            JLabel trendLabel = new JLabel(trendResult.getMessage());
            trendLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            trendPanel.add(trendLabel, BorderLayout.CENTER);
            
            // Add status icon
            JLabel statusIcon = new JLabel();
            if (trendResult.isSignificant()) {
                if (trendResult.isImproving()) {
                    statusIcon.setText("↑ IMPROVING");
                    statusIcon.setForeground(Color.GREEN.darker());
                } else {
                    statusIcon.setText("↓ DECLINING");
                    statusIcon.setForeground(Color.RED);
                }
            } else {
                statusIcon.setText("→ STABLE");
                statusIcon.setForeground(Color.BLUE);
            }
            statusIcon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            statusIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            trendPanel.add(statusIcon, BorderLayout.SOUTH);
            
            // Add to results panel
            resultsPanel.add(horseInfoPanel, BorderLayout.NORTH);
            resultsPanel.add(trendPanel, BorderLayout.CENTER);
            
            // Show results
            JOptionPane.showMessageDialog(
                parentFrame,
                resultsPanel,
                "Performance Analysis: " + selectedHorse.getName(),
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    /**
     * Open the statistics viewer
     */
    private void openStatisticsViewer() {
        SwingUtilities.invokeLater(() -> {
            StatisticsViewer viewer = new StatisticsViewer();
            viewer.setVisible(true);
        });
    }
    
    /**
     * Export statistics to files
     */
/**
     * Export statistics to files
     */
    private void exportStatistics() {
        // Open a dialog to choose what to export
        String[] options = {
            "Export Horse Statistics",
            "Export Track Records",
            "Export Single Horse History",
            "Cancel"
        };
        
        int choice = JOptionPane.showOptionDialog(
            parentFrame,
            "Select what to export:",
            "Export Statistics",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        // Handle the export based on the user's choice
        switch (choice) {
            case 0: // Horse Statistics
                exportHorseStatistics();
                break;
            case 1: // Track Records
                exportTrackRecords();
                break;
            case 2: // Single Horse History
                exportSingleHorseHistory();
                break;
        }
    }

    /**
     * Export horse statistics to a CSV file
     */
    private void exportHorseStatistics() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Horse Statistics");
        fileChooser.setSelectedFile(new File("horse_statistics.csv"));
        
        int result = fileChooser.showSaveDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            boolean success = StatisticsExporter.exportHorseStatisticsToCSV(filePath);
            
            if (success) {
                JOptionPane.showMessageDialog(parentFrame, "Statistics exported successfully");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Failed to export statistics");
            }
        }
    }

    /**
     * Export track records to a CSV file
     */
    private void exportTrackRecords() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Track Records");
        fileChooser.setSelectedFile(new File("track_records.csv"));
        
        int result = fileChooser.showSaveDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            boolean success = StatisticsExporter.exportTrackRecordsToCSV(filePath);
            
            if (success) {
                JOptionPane.showMessageDialog(parentFrame, "Track records exported successfully");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Failed to export track records");
            }
        }
    }

    /**
     * Export a single horse's race history
     */
    private void exportSingleHorseHistory() {
        // First select a horse
        List<Horse> horses = parentFrame.getRaceManager().getHorses();
        if (horses.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "No horses available");
            return;
        }
        
        Horse selectedHorse = (Horse) JOptionPane.showInputDialog(
            parentFrame,
            "Select a horse",
            "Export Horse History",
            JOptionPane.QUESTION_MESSAGE,
            null,
            horses.toArray(),
            horses.get(0)
        );
        
        if (selectedHorse != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Horse History");
            fileChooser.setSelectedFile(new File(selectedHorse.getName() + "_history.csv"));
            
            int result = fileChooser.showSaveDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                
                boolean success = StatisticsExporter.exportHorseRaceHistoryToCSV(selectedHorse, filePath);
                
                if (success) {
                    JOptionPane.showMessageDialog(parentFrame, "Horse history exported successfully");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Failed to export horse history");
                }
            }
        }
    }
    
    /**
     * Initialize test data for the statistics system
     */
    private void initializeTestData() {
        // Initialize test data using the race manager
        parentFrame.getRaceManager().initializeTestData();
        
        JOptionPane.showMessageDialog(
            parentFrame,
            "Test statistics data initialized.\nYou can now view statistics.",
            "Test Data Initialized",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}