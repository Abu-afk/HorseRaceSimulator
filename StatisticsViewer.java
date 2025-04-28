import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.awt.geom.*;

/**
 * StatisticsViewer provides a graphical interface for viewing and analyzing
 * horse racing statistics. It uses only Swing components (no JavaFX).
 * 
 * This class integrates with the existing statistics system and provides:
 * - Horse performance dashboards
 * - Track record viewing
 * - Horse comparison tools
 * - Performance trend visualization
 * 
 * @author Your Name
 * @version 1.0
 */
public class StatisticsViewer extends JFrame {
    // Formatting
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    // Reference to the statistics manager
    private StatisticsManager statsManager;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JPanel horseStatsPanel;
    private JPanel trackRecordsPanel;
    private JPanel compareHorsesPanel;
    private JPanel trendAnalysisPanel;
    
    // Horse selection components
    private JComboBox<Horse> horseSelector;
    private JList<Horse> horseComparisonList;
    private DefaultListModel<Horse> horseListModel;
    
    /**
     * Constructor for StatisticsViewer
     */
    public StatisticsViewer() {
        super("Horse Racing Statistics Viewer");
        
        // Get the statistics manager instance
        statsManager = StatisticsManager.getInstance();
        
        // Set up the UI
        setupUI();
        
        // Configure the JFrame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null); // Center on screen
    }
    
    /**
     * Set up the user interface
     */
    private void setupUI() {
        // Create tabbed pane for different statistics views
        tabbedPane = new JTabbedPane();
        
        // Set up each panel
        setupHorseStatsPanel();
        setupTrackRecordsPanel();
        setupCompareHorsesPanel();
        setupTrendAnalysisPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Horse Statistics", new JScrollPane(horseStatsPanel));
        tabbedPane.addTab("Track Records", new JScrollPane(trackRecordsPanel));
        tabbedPane.addTab("Compare Horses", new JScrollPane(compareHorsesPanel));
        tabbedPane.addTab("Trend Analysis", new JScrollPane(trendAnalysisPanel));
        
        // Add tabbed pane to frame
        add(tabbedPane);
        
        // Add refresh button at the bottom
        JButton refreshButton = new JButton("Refresh Statistics");
        refreshButton.addActionListener(e -> refreshAllPanels());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Set up the horse statistics panel
     */
    private void setupHorseStatsPanel() {
        horseStatsPanel = new JPanel();
        horseStatsPanel.setLayout(new BorderLayout());
        
        // Create selection panel at the top
        JPanel selectionPanel = new JPanel();
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Horse"));
        
        // Horse selector
        horseSelector = new JComboBox<>();
        updateHorseSelector();
        
        // Selection controls
        selectionPanel.add(new JLabel("Horse:"));
        selectionPanel.add(horseSelector);
        
        JButton viewButton = new JButton("View Statistics");
        viewButton.addActionListener(e -> updateHorseStatistics());
        selectionPanel.add(viewButton);
        
        horseStatsPanel.add(selectionPanel, BorderLayout.NORTH);
        
        // Create content panel for horse statistics
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add placeholder content
        contentPanel.add(new JLabel("Select a horse and click 'View Statistics'"));
        
        horseStatsPanel.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }
    
    /**
     * Update the horse selector with all available horses
     */
    private void updateHorseSelector() {
        horseSelector.removeAllItems();
        
        List<HorseStatistics> allStats = statsManager.getAllHorseStatistics();
        if (allStats.isEmpty()) {
            horseSelector.addItem(null); // No horses available
        } else {
            for (HorseStatistics stats : allStats) {
                horseSelector.addItem(stats.getHorse());
            }
        }
    }
    
    /**
     * Update the horse statistics display for the selected horse
     */
    private void updateHorseStatistics() {
        Horse selectedHorse = (Horse) horseSelector.getSelectedItem();
        if (selectedHorse == null) return;
        
        HorseStatistics stats = statsManager.getHorseStatistics(selectedHorse);
        if (stats == null) return;
        
        // Remove old content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Horse basic info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Horse Information"));
        
        // Main info in a grid
        JPanel mainInfoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        mainInfoPanel.add(new JLabel("Name:"));
        mainInfoPanel.add(new JLabel(selectedHorse.getName()));
        mainInfoPanel.add(new JLabel("Breed:"));
        mainInfoPanel.add(new JLabel(selectedHorse.getBreed().getName()));
        mainInfoPanel.add(new JLabel("Current Confidence:"));
        mainInfoPanel.add(new JLabel(DECIMAL_FORMAT.format(selectedHorse.getConfidence())));
        
        infoPanel.add(mainInfoPanel, BorderLayout.CENTER);
        
        // Add detailed stats
        JPanel performancePanel = new JPanel();
        performancePanel.setLayout(new BoxLayout(performancePanel, BoxLayout.Y_AXIS));
        performancePanel.setBorder(BorderFactory.createTitledBorder("Performance Statistics"));
        
        // Stats grid
        JPanel statsGrid = new JPanel(new GridLayout(0, 2, 5, 5));
        statsGrid.add(new JLabel("Total Races:"));
        statsGrid.add(new JLabel(String.valueOf(stats.getTotalRaces())));
        statsGrid.add(new JLabel("Wins:"));
        statsGrid.add(new JLabel(String.valueOf(stats.getWins())));
        statsGrid.add(new JLabel("Win Ratio:"));
        statsGrid.add(new JLabel(DECIMAL_FORMAT.format(stats.getWinRatio() * 100) + "%"));
        statsGrid.add(new JLabel("Completed Races:"));
        statsGrid.add(new JLabel(String.valueOf(stats.getCompletedRaces())));
        statsGrid.add(new JLabel("Falls:"));
        statsGrid.add(new JLabel(String.valueOf(stats.getFalls())));
        statsGrid.add(new JLabel("Average Position:"));
        statsGrid.add(new JLabel(DECIMAL_FORMAT.format(stats.getAveragePosition())));
        statsGrid.add(new JLabel("Average Speed:"));
        statsGrid.add(new JLabel(DECIMAL_FORMAT.format(stats.getAverageSpeed()) + " units/sec"));
        statsGrid.add(new JLabel("Best Time:"));
        statsGrid.add(new JLabel(RaceStatistics.formatTime(stats.getBestTime())));
        statsGrid.add(new JLabel("Average Time:"));
        statsGrid.add(new JLabel(RaceStatistics.formatTime(stats.getAverageTime())));
        
        performancePanel.add(statsGrid);
        
        // Race history in a table
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Race History"));
        
        // Create table model
        String[] columnNames = {"Date", "Track", "Condition", "Position", "Time", "Speed", "Distance", "Fallen", "Confidence Change"};
        Object[][] data = new Object[stats.getRaceHistory().size()][columnNames.length];
        
        List<HorseStatistics.RaceResult> history = stats.getRaceHistory();
        for (int i = 0; i < history.size(); i++) {
            HorseStatistics.RaceResult result = history.get(i);
            data[i][0] = DATE_FORMAT.format(result.getRaceDate());
            data[i][1] = result.getTrackName();
            data[i][2] = result.getTrackCondition().getName();
            data[i][3] = result.getPosition() > 0 ? String.valueOf(result.getPosition()) : "DNF";
            data[i][4] = RaceStatistics.formatTime(result.getFinishTime());
            data[i][5] = DECIMAL_FORMAT.format(result.getAverageSpeed());
            data[i][6] = DECIMAL_FORMAT.format(result.getDistanceTravelled()) + " (" + 
                         DECIMAL_FORMAT.format(result.getCompletionPercentage()) + "%)";
            data[i][7] = result.hasFallen() ? "Yes" : "No";
            data[i][8] = DECIMAL_FORMAT.format(result.getConfidenceChange());
        }
        
        JTable historyTable = new JTable(data, columnNames);
        historyTable.setFillsViewportHeight(true);
        historyTable.setAutoCreateRowSorter(true);
        
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        // Add all panels to content panel
        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(performancePanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(historyPanel);
        
        // Create performance graph
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBorder(BorderFactory.createTitledBorder("Performance Graph"));
        
        if (history.size() >= 2) {
            PerformanceGraph graph = new PerformanceGraph(history);
            graph.setPreferredSize(new Dimension(600, 200));
            graphPanel.add(graph, BorderLayout.CENTER);
            
            // Legend
            JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            legendPanel.add(createLegendItem("Position", Color.BLUE));
            legendPanel.add(createLegendItem("Speed", Color.RED));
            legendPanel.add(createLegendItem("Confidence", Color.GREEN));
            graphPanel.add(legendPanel, BorderLayout.SOUTH);
        } else {
            graphPanel.add(new JLabel("Not enough race history to display graph"), BorderLayout.CENTER);
        }
        
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(graphPanel);
        
        // Update the display
        horseStatsPanel.remove(1); // Remove old content
        horseStatsPanel.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        horseStatsPanel.revalidate();
        horseStatsPanel.repaint();
    }
    
    /**
     * Create a legend item for the graph
     */
    private JPanel createLegendItem(String label, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(16, 16));
        panel.add(colorBox);
        panel.add(new JLabel(label));
        return panel;
    }
    
    /**
     * Set up the track records panel
     */
    private void setupTrackRecordsPanel() {
        trackRecordsPanel = new JPanel(new BorderLayout());
        
        // Create header
        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(BorderFactory.createTitledBorder("Track Records"));
        headerPanel.add(new JLabel("All track records and best performances"));
        
        trackRecordsPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create records table
        String[] columnNames = {"Track", "Condition", "Best Time", "Record Holder", "Date"};
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        JTable recordsTable = new JTable(model);
        recordsTable.setFillsViewportHeight(true);
        recordsTable.setAutoCreateRowSorter(true);
        
        // Add track records to table
        updateTrackRecordsTable(model);
        
        trackRecordsPanel.add(new JScrollPane(recordsTable), BorderLayout.CENTER);
    }
    
    /**
     * Update the track records table with current data
     */
    private void updateTrackRecordsTable(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows
        
        List<StatisticsManager.TrackRecord> records = statsManager.getAllTrackRecords();
        for (StatisticsManager.TrackRecord record : records) {
            if (record.getBestTime() > 0 && record.getRecordHolder() != null) {
                model.addRow(new Object[]{
                    record.getTrackName(),
                    record.getTrackCondition().getName(),
                    RaceStatistics.formatTime(record.getBestTime()),
                    record.getRecordHolder().getName(),
                    record.getRecordDate() != null ? DATE_FORMAT.format(record.getRecordDate()) : "N/A"
                });
            }
        }
    }
    
    /**
     * Set up the horse comparison panel
     */
    private void setupCompareHorsesPanel() {
        compareHorsesPanel = new JPanel(new BorderLayout());
        
        // Create selection panel
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Select Horses to Compare"));
        
        // Create list for selected horses
        horseListModel = new DefaultListModel<>();
        horseComparisonList = new JList<>(horseListModel);
        horseComparisonList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        JScrollPane listScrollPane = new JScrollPane(horseComparisonList);
        listScrollPane.setPreferredSize(new Dimension(200, 150));
        
        // Create dropdown to add horses
        JComboBox<Horse> addHorseCombo = new JComboBox<>();
        updateComparisonHorseSelector(addHorseCombo);
        
        // Add button
        JButton addButton = new JButton("Add Horse");
        addButton.addActionListener(e -> {
            Horse selected = (Horse)addHorseCombo.getSelectedItem();
            if (selected != null && !isHorseInList(selected)) {
                horseListModel.addElement(selected);
            }
        });
        
        // Remove button
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> {
            List<Horse> selected = horseComparisonList.getSelectedValuesList();
            for (Horse horse : selected) {
                horseListModel.removeElement(horse);
            }
        });
        
        // Compare button
        JButton compareButton = new JButton("Compare Horses");
        compareButton.addActionListener(e -> updateComparisonTable());
        
        // Controls panel
        JPanel controlsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        controlsPanel.add(addHorseCombo);
        controlsPanel.add(addButton);
        controlsPanel.add(removeButton);
        controlsPanel.add(compareButton);
        
        // Add components to selection panel
        selectionPanel.add(listScrollPane, BorderLayout.CENTER);
        selectionPanel.add(controlsPanel, BorderLayout.EAST);
        
        // Add selection panel to main panel
        compareHorsesPanel.add(selectionPanel, BorderLayout.NORTH);
        
        // Add placeholder for comparison results
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.add(new JLabel("Select horses and click 'Compare Horses'"));
        
        compareHorsesPanel.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);
    }
    
    /**
     * Check if a horse is already in the comparison list
     */
    private boolean isHorseInList(Horse horse) {
        for (int i = 0; i < horseListModel.size(); i++) {
            if (horseListModel.get(i).getName().equals(horse.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Update the horse selector for comparison
     */
    private void updateComparisonHorseSelector(JComboBox<Horse> comboBox) {
        comboBox.removeAllItems();
        
        List<HorseStatistics> allStats = statsManager.getAllHorseStatistics();
        if (allStats.isEmpty()) {
            comboBox.addItem(null); // No horses available
        } else {
            for (HorseStatistics stats : allStats) {
                comboBox.addItem(stats.getHorse());
            }
        }
    }
    
    /**
     * Update the comparison table
     */
    private void updateComparisonTable() {
        if (horseListModel.isEmpty()) return;
        
        // Create results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        
        // List of horses to compare
        List<Horse> horsesToCompare = new ArrayList<>();
        for (int i = 0; i < horseListModel.size(); i++) {
            horsesToCompare.add(horseListModel.get(i));
        }
        
        // Create table model
        String[] columnNames = new String[horsesToCompare.size() + 1];
        columnNames[0] = "Statistic";
        for (int i = 0; i < horsesToCompare.size(); i++) {
            columnNames[i+1] = horsesToCompare.get(i).getName();
        }
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Add statistics rows
        model.addRow(createComparisonRow("Breed", horsesToCompare, h -> h.getBreed().getName()));
        model.addRow(createComparisonRow("Current Confidence", horsesToCompare, h -> DECIMAL_FORMAT.format(h.getConfidence())));
        
        // Add performance statistics
        for (Horse horse : horsesToCompare) {
            if (statsManager.getHorseStatistics(horse) == null) {
                // If any horse doesn't have statistics, show warning and return
                resultsPanel.add(new JLabel("No statistics available for " + horse.getName()));
                compareHorsesPanel.remove(1); // Remove old results
                compareHorsesPanel.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);
                compareHorsesPanel.revalidate();
                compareHorsesPanel.repaint();
                return;
            }
        }
        
        // All horses have statistics, add performance rows
        model.addRow(createComparisonRow("Total Races", horsesToCompare, 
            h -> String.valueOf(statsManager.getHorseStatistics(h).getTotalRaces())));
        model.addRow(createComparisonRow("Wins", horsesToCompare, 
            h -> String.valueOf(statsManager.getHorseStatistics(h).getWins())));
        model.addRow(createComparisonRow("Win Ratio", horsesToCompare, 
            h -> DECIMAL_FORMAT.format(statsManager.getHorseStatistics(h).getWinRatio() * 100) + "%"));
        model.addRow(createComparisonRow("Falls", horsesToCompare, 
            h -> String.valueOf(statsManager.getHorseStatistics(h).getFalls())));
        model.addRow(createComparisonRow("Average Position", horsesToCompare, 
            h -> DECIMAL_FORMAT.format(statsManager.getHorseStatistics(h).getAveragePosition())));
        model.addRow(createComparisonRow("Average Speed", horsesToCompare, 
            h -> DECIMAL_FORMAT.format(statsManager.getHorseStatistics(h).getAverageSpeed()) + " units/sec"));
        model.addRow(createComparisonRow("Best Time", horsesToCompare, 
            h -> RaceStatistics.formatTime(statsManager.getHorseStatistics(h).getBestTime())));
        
        // Create and add the comparison table
        JTable comparisonTable = new JTable(model);
        comparisonTable.setFillsViewportHeight(true);
        
        // Add table to panel
        resultsPanel.add(new JScrollPane(comparisonTable));
        
        // Create comparison chart
        ChartPanel chartPanel = new ChartPanel(horsesToCompare);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Comparison Chart"));
        chartPanel.setPreferredSize(new Dimension(600, 300));
        
        resultsPanel.add(Box.createVerticalStrut(10));
        resultsPanel.add(chartPanel);
        
        // Replace old results with new ones
        compareHorsesPanel.remove(1); // Remove old results
        compareHorsesPanel.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);
        compareHorsesPanel.revalidate();
        compareHorsesPanel.repaint();
    }
    
    /**
     * Create a row for the comparison table
     */
    private Object[] createComparisonRow(String statName, List<Horse> horses, HorseStatFunction statFunction) {
        Object[] row = new Object[horses.size() + 1];
        row[0] = statName;
        for (int i = 0; i < horses.size(); i++) {
            row[i+1] = statFunction.apply(horses.get(i));
        }
        return row;
    }
    
    /**
     * Functional interface for retrieving horse statistics
     */
    @FunctionalInterface
    private interface HorseStatFunction {
        String apply(Horse horse);
    }
    
    /**
     * Set up the trend analysis panel
     */
    private void setupTrendAnalysisPanel() {
        trendAnalysisPanel = new JPanel(new BorderLayout());
        
        // Create header
        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(BorderFactory.createTitledBorder("Trend Analysis"));
        headerPanel.add(new JLabel("Performance trends for all horses"));
        
        trendAnalysisPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create panels for different trend categories
        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
        
        // Top performers panel
        JPanel topPerformersPanel = createTrendPanel("Top Performing Horses (by Win Ratio)", 
            () -> statsManager.getTopPerformingHorses(5));
        
        // Fastest horses panel
        JPanel fastestHorsesPanel = createTrendPanel("Fastest Horses (by Average Speed)",
            () -> statsManager.getFastestHorses(5));
        
        // Most improved horses panel
        JPanel improvedHorsesPanel = createTrendPanel("Most Improved Horses",
            () -> statsManager.getMostImprovedHorses(5));
        
        // Add panels to overall panel
        overallPanel.add(topPerformersPanel);
        overallPanel.add(Box.createVerticalStrut(20));
        overallPanel.add(fastestHorsesPanel);
        overallPanel.add(Box.createVerticalStrut(20));
        overallPanel.add(improvedHorsesPanel);
        
        // Add overall panel to trend analysis panel
        trendAnalysisPanel.add(new JScrollPane(overallPanel), BorderLayout.CENTER);
    }
    
    /**
     * Create a panel for a specific trend category
     */
    private JPanel createTrendPanel(String title, Supplier<List<HorseStatistics>> statsSupplier) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        String[] columnNames = {"Horse", "Breed", "Races", "Wins", "Win Ratio", "Avg Speed", "Best Time"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Add data to table
        List<HorseStatistics> statsList = statsSupplier.get();
        for (HorseStatistics stats : statsList) {
            Horse horse = stats.getHorse();
            model.addRow(new Object[]{
                horse.getName(),
                horse.getBreed().getName(),
                stats.getTotalRaces(),
                stats.getWins(),
                DECIMAL_FORMAT.format(stats.getWinRatio() * 100) + "%",
                DECIMAL_FORMAT.format(stats.getAverageSpeed()) + " units/sec",
                RaceStatistics.formatTime(stats.getBestTime())
            });
        }
        
        // If no data, add a message
        if (statsList.isEmpty()) {
            JLabel noDataLabel = new JLabel("No data available for this category");
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(noDataLabel, BorderLayout.CENTER);
        } else {
            JTable table = new JTable(model);
            table.setFillsViewportHeight(true);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    /**
     * Refresh all panels with current data
     */
    private void refreshAllPanels() {
        // Update horse selectors
        updateHorseSelector();
        
        // Refresh track records panel
        JTable recordsTable = (JTable) ((JScrollPane) trackRecordsPanel.getComponent(1)).getViewport().getView();
        updateTrackRecordsTable((DefaultTableModel) recordsTable.getModel());
        
        // Refresh comparison panel horse selector
        JComboBox<Horse> addHorseCombo = (JComboBox<Horse>) 
            ((JPanel) ((BorderLayout) ((JPanel) compareHorsesPanel.getComponent(0)).getLayout()).getLayoutComponent(BorderLayout.EAST)).getComponent(0);
        updateComparisonHorseSelector(addHorseCombo);
        
        // Refresh trend analysis panel
        tabbedPane.remove(3); // Remove old trend analysis panel
        setupTrendAnalysisPanel();
        tabbedPane.addTab("Trend Analysis", new JScrollPane(trendAnalysisPanel));
        
        // Update horse statistics if a horse is selected
        if (horseSelector.getSelectedItem() != null) {
            updateHorseStatistics();
        }
        
        // Update comparison if horses are selected
        if (horseListModel.size() > 0) {
            updateComparisonTable();
        }
    }
    
    /**
     * Supplier functional interface
     */
    @FunctionalInterface
    private interface Supplier<T> {
        T get();
    }
    
    /**
     * Custom panel for displaying performance graphs
     */
    private class PerformanceGraph extends JPanel {
        private List<HorseStatistics.RaceResult> raceHistory;
        
        public PerformanceGraph(List<HorseStatistics.RaceResult> history) {
            this.raceHistory = new ArrayList<>(history);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (raceHistory.size() < 2) return;
            
            int width = getWidth() - 40;
            int height = getHeight() - 40;
            int leftMargin = 30;
            int topMargin = 20;
            
            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(leftMargin, topMargin, leftMargin, topMargin + height);
            g2d.drawLine(leftMargin, topMargin + height, leftMargin + width, topMargin + height);
            
            // Calculate interval between points
            int interval = width / (raceHistory.size() - 1);
            
            // Find maximum values for scaling
            int maxPosition = 0;
            double maxSpeed = 0;
            double maxConfidence = 0;

            for (HorseStatistics.RaceResult result : raceHistory) {
                maxPosition = Math.max(maxPosition, result.getPosition());
                maxSpeed = Math.max(maxSpeed, result.getAverageSpeed());
                maxConfidence = Math.max(maxConfidence, result.getConfidenceAfter());
            }

            // Ensure reasonable maximum values
            maxPosition = Math.max(maxPosition, 5);
            maxSpeed = Math.max(maxSpeed, 1.0);
            maxConfidence = Math.max(maxConfidence, 1.0);

            // Create a final copy for use in lambda
            final int finalMaxPosition = maxPosition;

            // Draw position line (blue)
            drawLine(g2d, raceHistory, leftMargin, topMargin, interval, height, maxPosition, 
                result -> result.getPosition() > 0 ? result.getPosition() : finalMaxPosition, Color.BLUE);
            
            // Draw speed line (red)
            drawLine(g2d, raceHistory, leftMargin, topMargin, interval, height, maxSpeed,
                result -> result.getAverageSpeed(), Color.RED);
            
            // Draw confidence line (green)
            drawLine(g2d, raceHistory, leftMargin, topMargin, interval, height, maxConfidence,
                result -> result.getConfidenceAfter(), Color.GREEN);
            
            // Draw labels and grid lines
            drawGridLines(g2d, leftMargin, topMargin, width, height, 5);
        }
        
        /**
         * Draw a line on the graph for a specific metric
         */
        private void drawLine(Graphics2D g2d, List<HorseStatistics.RaceResult> history, 
                             int leftMargin, int topMargin, int interval, int height, 
                             double maxValue, ResultValueFunction valueFunction, Color color) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            
            int[] xPoints = new int[history.size()];
            int[] yPoints = new int[history.size()];
            
            for (int i = 0; i < history.size(); i++) {
                HorseStatistics.RaceResult result = history.get(i);
                double value = valueFunction.apply(result);
                
                xPoints[i] = leftMargin + (i * interval);
                yPoints[i] = topMargin + height - (int)((value / maxValue) * height);
            }
            
            for (int i = 0; i < xPoints.length - 1; i++) {
                g2d.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
            }
            
            // Draw points
            for (int i = 0; i < xPoints.length; i++) {
                g2d.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
            }
        }
        
        /**
         * Draw grid lines and labels
         */
        private void drawGridLines(Graphics2D g2d, int leftMargin, int topMargin, 
                                  int width, int height, int divisions) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
                                         0, new float[]{2}, 0));
            
            // Draw horizontal grid lines
            for (int i = 1; i < divisions; i++) {
                int y = topMargin + (height * i / divisions);
                g2d.drawLine(leftMargin, y, leftMargin + width, y);
            }
            
            // Draw vertical grid lines for each race
            for (int i = 0; i < raceHistory.size(); i++) {
                int x = leftMargin + (i * width / (raceHistory.size() - 1));
                g2d.drawLine(x, topMargin, x, topMargin + height);
            }
            
            // Draw labels
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            
            for (int i = 0; i < raceHistory.size(); i++) {
                int x = leftMargin + (i * width / (raceHistory.size() - 1));
                g2d.drawString(String.valueOf(i+1), x - 3, topMargin + height + 15);
            }
        }
    }
    
    /**
     * Functional interface for retrieving result values
     */
    @FunctionalInterface
    private interface ResultValueFunction {
        double apply(HorseStatistics.RaceResult result);
    }
    
    /**
     * Custom panel for displaying comparison charts
     */
    private class ChartPanel extends JPanel {
        private List<Horse> horses;
        
        public ChartPanel(List<Horse> horses) {
            this.horses = new ArrayList<>(horses);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (horses.isEmpty()) return;
            
            int width = getWidth() - 80;
            int height = getHeight() - 60;
            int leftMargin = 60;
            int topMargin = 20;
            int barWidth = Math.min(50, width / (horses.size() * 3));
            int gap = barWidth / 2;
            
            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(leftMargin, topMargin, leftMargin, topMargin + height);
            g2d.drawLine(leftMargin, topMargin + height, leftMargin + width, topMargin + height);
            
            // Find maximum values for scaling
            double maxWinRatio = 0;
            double maxSpeed = 0;
            
            for (Horse horse : horses) {
                HorseStatistics stats = statsManager.getHorseStatistics(horse);
                if (stats != null) {
                    maxWinRatio = Math.max(maxWinRatio, stats.getWinRatio());
                    maxSpeed = Math.max(maxSpeed, stats.getAverageSpeed());
                }
            }
            
            // Ensure reasonable maximum values
            maxWinRatio = Math.max(maxWinRatio, 0.1);
            maxSpeed = Math.max(maxSpeed, 1.0);
            
            // Draw bars for each horse
            int x = leftMargin + gap;
            
            for (int i = 0; i < horses.size(); i++) {
                Horse horse = horses.get(i);
                HorseStatistics stats = statsManager.getHorseStatistics(horse);
                
                if (stats != null) {
                    // Win ratio bar (blue)
                    int winRatioHeight = (int)((stats.getWinRatio() / maxWinRatio) * height);
                    g2d.setColor(new Color(0, 0, 200, 180));
                    g2d.fillRect(x, topMargin + height - winRatioHeight, barWidth, winRatioHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, topMargin + height - winRatioHeight, barWidth, winRatioHeight);
                    
                    // Speed bar (red)
                    int speedHeight = (int)((stats.getAverageSpeed() / maxSpeed) * height);
                    g2d.setColor(new Color(200, 0, 0, 180));
                    g2d.fillRect(x + barWidth + gap, topMargin + height - speedHeight, barWidth, speedHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x + barWidth + gap, topMargin + height - speedHeight, barWidth, speedHeight);
                    
                    // Confidence bar (green)
                    int confidenceHeight = (int)(horse.getConfidence() * height);
                    g2d.setColor(new Color(0, 150, 0, 180));
                    g2d.fillRect(x + 2 * barWidth + 2 * gap, topMargin + height - confidenceHeight, barWidth, confidenceHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x + 2 * barWidth + 2 * gap, topMargin + height - confidenceHeight, barWidth, confidenceHeight);
                    
                    // Horse name
                    g2d.setColor(Color.BLACK);
                    String shortName = shortenText(horse.getName(), 10);
                    FontMetrics fm = g2d.getFontMetrics();
                    int nameWidth = fm.stringWidth(shortName);
                    int nameX = x + barWidth + gap - nameWidth / 2;
                    g2d.drawString(shortName, nameX, topMargin + height + 15);
                }
                
                x += 3 * barWidth + 4 * gap;
            }
            
            // Draw legend
            int legendX = leftMargin;
            int legendY = topMargin + height + 30;
            
            g2d.setColor(new Color(0, 0, 200, 180));
            g2d.fillRect(legendX, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, legendY, 15, 15);
            g2d.drawString("Win Ratio", legendX + 20, legendY + 12);
            
            legendX += 100;
            g2d.setColor(new Color(200, 0, 0, 180));
            g2d.fillRect(legendX, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, legendY, 15, 15);
            g2d.drawString("Avg Speed", legendX + 20, legendY + 12);
            
            legendX += 100;
            g2d.setColor(new Color(0, 150, 0, 180));
            g2d.fillRect(legendX, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, legendY, 15, 15);
            g2d.drawString("Confidence", legendX + 20, legendY + 12);
            
            // Draw scale on y-axis
            g2d.setColor(Color.BLACK);
            for (int i = 0; i <= 10; i++) {
                int y = topMargin + height - (i * height / 10);
                g2d.drawLine(leftMargin - 5, y, leftMargin, y);
                g2d.drawString(String.format("%.1f", i / 10.0), leftMargin - 30, y + 5);
            }
        }
        
        /**
         * Shorten text to fit in the chart
         */
        private String shortenText(String text, int maxLength) {
            if (text.length() <= maxLength) {
                return text;
            } else {
                return text.substring(0, maxLength - 3) + "...";
            }
        }
    }
    
    /**
     * Main method to run the statistics viewer
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the statistics viewer
        SwingUtilities.invokeLater(() -> {
            StatisticsViewer viewer = new StatisticsViewer();
            viewer.setVisible(true);
        });
    }
}