import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.File;
import javax.swing.table.DefaultTableModel;
import java.text.DecimalFormat;

/**
 * The main GUI class for the Horse Racing application.
 * It provides interfaces for track customization, horse management, and race visualization.
 * 
 * This version includes support for customizable horses.
 * 
 * @author (Your Name)
 * @version (2.0)
 */
public class RacingGUI extends JFrame {
    private RaceManager raceManager;    // The race manager
    private JPanel controlPanel;        // Panel for controls
    private RaceTrackPanel trackPanel; // Panel for displaying the race track
    private BettingPanel bettingPanel;  
    private JButton startButton;        // Button to start the race
    private JButton resetButton;        // Button to reset the race
    private JComboBox<String> trackTypeCombo; // Combo box for track type selection
    private JSlider lanesSlider;        // Slider for number of lanes
    private JSlider lengthSlider;       // Slider for track length
    private JComboBox<TrackCondition> conditionCombo; // Combo box for track condition
    private JPanel horsePanel;          // Panel for managing horses
    
    private Map<Horse, Color> horseColors; // Colors for each horse
    
    /**
     * Get the race manager
     */
    public RaceManager getRaceManager() {
        return raceManager;
    }
    
    /**
     * Constructor for the RacingGUI class
     */
    public RacingGUI() {
        super("Horse Racing Simulator");
        raceManager = new RaceManager();
        RaceManagerSingleton.setInstance(raceManager);
        horseColors = new HashMap<>();
        
        // Set up the GUI components
        setupUI();
        
        // Add race listener to update the UI when race events occur
        raceManager.addRaceListener(new RaceManager.RaceListener() {
            @Override
            public void onRaceStart() {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(false);
                    resetButton.setEnabled(true);
                    trackTypeCombo.setEnabled(false);
                    lanesSlider.setEnabled(false);
                    lengthSlider.setEnabled(false);
                    conditionCombo.setEnabled(false);
                    BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
                });
            }
            
            @Override
            public void onHorseFallen(Horse horse) {
                // Just update the UI, no special handling needed here
            }
            
            @Override
            public void onRaceWinner(Horse horse) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(RacingGUI.this, 
                        horse.getName() + " is the winner!", 
                        "Race Finished", 
                        JOptionPane.INFORMATION_MESSAGE);
                    try {
                        BettingService.getInstance().settleRace(horse);
                    } catch (Exception ex) {
                        System.err.println("Error settling race: " + ex.getMessage());
                    }
                });
            }
            
            @Override
            public void onRaceUpdate() {
                SwingUtilities.invokeLater(() -> {
                    trackPanel.repaint();
                });
            }
            
            @Override
            public void onRaceEnd(Horse winner) {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    resetButton.setEnabled(false);
                    trackTypeCombo.setEnabled(true);
                    lanesSlider.setEnabled(true);
                    lengthSlider.setEnabled(true);
                    conditionCombo.setEnabled(true);
                    
                    // Update horse panel to show new confidence values
                    if (winner == null) {
                        BettingService.getInstance().endRace();
                    }
                    updateHorsePanel();
                    // Record race statistics
                    raceManager.recordRaceStatistics();

                    // Add this line to reset the race state
                    raceManager.resetRaceState();
                    // Update betting service with current horses and track
                    if (!raceManager.isRaceInProgress()) {
                        BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
                    }
                });
            }
        });
        
        // Configure the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null); // Center on screen
        // Force menu bar to be visible - add this right before setVisible(true)
        if (getJMenuBar() != null) {
            getJMenuBar().setVisible(true);
            // Try to force a repaint of the menu area
            validate();
            repaint();
            System.out.println("Forced menu bar visibility");
        }
        setVisible(true);
    }
    
    private void setupUI() {
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Add a basic File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Exit"));
        menuBar.add(fileMenu);

        // Add the statistics menu
        StatisticsMenu statsMenu = new StatisticsMenu(this);
        menuBar.add(statsMenu);
        
        // Add Betting menu
        JMenu bettingMenu = new JMenu("Betting");
        menuBar.add(bettingMenu);

        JMenuItem resetWalletItem = new JMenuItem("Reset Wallet");
        resetWalletItem.addActionListener(e -> {
            BettingService.getInstance().getWallet().reset();
            JOptionPane.showMessageDialog(this, 
                "Wallet reset to initial balance", 
                "Reset Wallet", 
                JOptionPane.INFORMATION_MESSAGE);
            if (bettingPanel != null) {
                bettingPanel.refreshUI();
            }
        });
        bettingMenu.add(resetWalletItem);

        JMenuItem viewHistoryItem = new JMenuItem("View Betting History");
        viewHistoryItem.addActionListener(e -> showBettingHistory());
        bettingMenu.add(viewHistoryItem);

        // Create the main panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        controlPanel = new JPanel();
        trackPanel = new RaceTrackPanel();
        
        // Set up the control panel
        setupControlPanel();
        
        // Add the panels to the main panel
        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(trackPanel, BorderLayout.CENTER);
        
        
        // Add the main panel to the frame
        add(mainPanel, BorderLayout.CENTER);  // CENTER instead of filling the whole frame
        
        bettingPanel = new BettingPanel();
        add(bettingPanel, BorderLayout.EAST);

        // Create example horses for testing
        createExampleHorses();
    }
    
    /**
     * Setup statistics menu
     */
    private void setupStatisticsMenu() {
        // Create statistics menu
        JMenu statsMenu = new JMenu("Statistics");
        
        // Quick Summary option
        JMenuItem quickSummaryItem = new JMenuItem("Quick Summary");
        quickSummaryItem.addActionListener(e -> showQuickSummary());
        statsMenu.add(quickSummaryItem);
        
        // Analyze Performance option
        JMenuItem analyzeItem = new JMenuItem("Analyze Performance");
        analyzeItem.addActionListener(e -> showPerformanceAnalyzer());
        statsMenu.add(analyzeItem);
        
        // View Statistics option
        JMenuItem viewStatsItem = new JMenuItem("View Full Statistics");
        viewStatsItem.addActionListener(e -> openStatisticsViewer());
        statsMenu.add(viewStatsItem);
        
        // Add to menu bar
        getJMenuBar().add(statsMenu);
        
        // Add buttons to control panel if desired
        JButton quickSummaryBtn = new JButton("Quick Summary");
        quickSummaryBtn.addActionListener(e -> showQuickSummary());
        
        JButton analyzeBtn = new JButton("Analyze Performance");
        analyzeBtn.addActionListener(e -> showPerformanceAnalyzer());
        
        // Add buttons to your control panel
        controlPanel.add(quickSummaryBtn);
        controlPanel.add(analyzeBtn);
    }
    
    /**
     * Initialize test data
     */
    private void initializeTestData() {
        // Initialize test data using race manager
        raceManager.initializeTestData();
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
                this,
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
            this,
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
                this,
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
            this,
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
                this,
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
        StatisticsViewer viewer = new StatisticsViewer();
        viewer.setVisible(true);
    }

    /**
     * Show betting history in a dialog
     */
    private void showBettingHistory() {
        BettingHistory history = BettingService.getInstance().getBettingHistory();
        List<Bet> bets = history.getAllBets();
        
        if (bets.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No bets have been placed yet", 
                "Betting History", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create table model
        String[] columns = {"Horse", "Amount", "Odds", "Status", "Payout"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Format for currency and odds
        DecimalFormat moneyFormat = new DecimalFormat("$#,##0.00");
        DecimalFormat oddsFormat = new DecimalFormat("#0.0");
        
        // Add rows to table
        for (Bet bet : bets) {
            String status = bet.isSettled() ? 
                (bet.isWon() ? "Won" : "Lost") : "Pending";
            String payout = bet.isWon() ? 
                moneyFormat.format(bet.getPayout()) : "-";
                
            Object[] row = {
                bet.getHorse().getName(),
                moneyFormat.format(bet.getAmount()),
                oddsFormat.format(bet.getOdds()) + ":1",
                status,
                payout
            };
            
            model.addRow(row);
        }
        
        // Create table and scroll pane
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        // Create summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(3, 2));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        
        double totalBets = history.getTotalBetAmount();
        double totalWinnings = history.getTotalWinnings();
        double netProfit = totalWinnings - totalBets;
        
        summaryPanel.add(new JLabel("Total Bets:"));
        summaryPanel.add(new JLabel(moneyFormat.format(totalBets)));
        summaryPanel.add(new JLabel("Total Winnings:"));
        summaryPanel.add(new JLabel(moneyFormat.format(totalWinnings)));
        summaryPanel.add(new JLabel("Net Profit:"));
        
        JLabel profitLabel = new JLabel(moneyFormat.format(netProfit));
        profitLabel.setForeground(netProfit >= 0 ? new Color(0, 100, 0) : Color.RED);
        summaryPanel.add(profitLabel);
        
        // Combine components
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        // Show dialog
        JOptionPane.showMessageDialog(this, panel, 
            "Betting History", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showQuickStats() {
        StatisticsManager manager = StatisticsManager.getInstance();
        String summary = manager.getStatisticsSummary();
        
        JTextArea textArea = new JTextArea(summary);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Statistics Summary",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void analyzePerformanceTrends() {
        // Open the statistics viewer to the performance trends tab
        SwingUtilities.invokeLater(() -> {
            StatisticsViewer viewer = new StatisticsViewer();
            viewer.setVisible(true);
        });
    }

    private void exportStatistics() {
        // Open a dialog to choose what to export
        String[] options = {
            "Export Horse Statistics",
            "Export Track Records",
            "Export Single Horse History",
            "Cancel"
        };
        
        int choice = JOptionPane.showOptionDialog(
            this,
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

    private void exportHorseStatistics() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Horse Statistics");
        fileChooser.setSelectedFile(new File("horse_statistics.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            boolean success = StatisticsExporter.exportHorseStatisticsToCSV(filePath);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Statistics exported successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export statistics");
            }
        }
    }

    private void exportTrackRecords() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Track Records");
        fileChooser.setSelectedFile(new File("track_records.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            boolean success = StatisticsExporter.exportTrackRecordsToCSV(filePath);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Track records exported successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export track records");
            }
        }
    }

    private void exportSingleHorseHistory() {
        // First select a horse
        List<Horse> horses = raceManager.getHorses();
        if (horses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No horses available");
            return;
        }
        
        Horse selectedHorse = (Horse) JOptionPane.showInputDialog(
            this,
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
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                
                boolean success = StatisticsExporter.exportHorseRaceHistoryToCSV(selectedHorse, filePath);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Horse history exported successfully");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to export horse history");
                }
            }
        }
    }
    
    /**
     * Set up the control panel with track and horse management controls
     */
    private void setupControlPanel() {
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(350, 600));

        // Track Configuration Panel
        JPanel trackConfigPanel = new JPanel();
        trackConfigPanel.setLayout(new BoxLayout(trackConfigPanel, BoxLayout.Y_AXIS));
        trackConfigPanel.setBorder(BorderFactory.createTitledBorder("Track Configuration"));
        
        // Track Type Selection
        JPanel trackTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        trackTypePanel.add(new JLabel("Track Type:"));
        trackTypeCombo = new JComboBox<>(new String[]{"Oval", "Figure-Eight", "Zigzag"});
        trackTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTrack();
            }
        });
        trackTypePanel.add(trackTypeCombo);
        trackConfigPanel.add(trackTypePanel);
        
        // Number of Lanes Slider
        JPanel lanesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lanesPanel.add(new JLabel("Lanes:"));
        lanesSlider = new JSlider(2, 8, 5);
        lanesSlider.setMajorTickSpacing(1);
        lanesSlider.setPaintTicks(true);
        lanesSlider.setPaintLabels(true);
        lanesSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!lanesSlider.getValueIsAdjusting()) {
                    updateTrack();
                }
            }
        });
        lanesPanel.add(lanesSlider);
        trackConfigPanel.add(lanesPanel);
        
        // Track Length Slider
        JPanel lengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lengthPanel.add(new JLabel("Length:"));
        lengthSlider = new JSlider(200, 1000, 500);
        lengthSlider.setMajorTickSpacing(200);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setPaintLabels(true);
        lengthSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!lengthSlider.getValueIsAdjusting()) {
                    updateTrack();
                }
            }
        });
        lengthPanel.add(lengthSlider);
        trackConfigPanel.add(lengthPanel);
        
        // Track Condition Selection
        JPanel conditionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        conditionPanel.add(new JLabel("Condition:"));
        conditionCombo = new JComboBox<>(new TrackCondition[]{
            TrackCondition.DRY, TrackCondition.MUDDY, 
            TrackCondition.ICY, TrackCondition.WET, TrackCondition.WINDY
        });
        conditionCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                raceManager.getTrack().setCondition((TrackCondition)conditionCombo.getSelectedItem());
                trackPanel.repaint();
            }
        });
        conditionPanel.add(conditionCombo);
        trackConfigPanel.add(conditionPanel);
        
        // Add the track config panel to the control panel
        controlPanel.add(trackConfigPanel);
        
        // Horse Management Panel
        horsePanel = new JPanel();
        horsePanel.setLayout(new BoxLayout(horsePanel, BoxLayout.Y_AXIS));
        horsePanel.setBorder(BorderFactory.createTitledBorder("Horses"));
        
        // Add a scrollable view for the horse panel
        JScrollPane horseScrollPane = new JScrollPane(horsePanel);
        horseScrollPane.setPreferredSize(new Dimension(330, 400));
        controlPanel.add(horseScrollPane);
        
        // Button to add a new horse
        JButton addHorseButton = new JButton("Add Horse");
        addHorseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewHorse();
            }
        });
        
        // Race Control Panel
        JPanel raceControlPanel = new JPanel(new FlowLayout());
        startButton = new JButton("Start Race");
        resetButton = new JButton("Reset");
        resetButton.setEnabled(false);
        
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                raceManager.startRace();
            }
        });
        
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                raceManager.stopRace();
                for (Horse horse : raceManager.getHorses()) {
                    horse.goBackToStart();
                }
                trackPanel.repaint();
                resetButton.setEnabled(false);
                startButton.setEnabled(true);
                trackTypeCombo.setEnabled(true);
                lanesSlider.setEnabled(true);
                lengthSlider.setEnabled(true);
                conditionCombo.setEnabled(true);
                
                // Add this line to reset the race state
                raceManager.resetRaceState();
                // Update betting service with current horses and track
                if (!raceManager.isRaceInProgress()) {
                    BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
                }
            }
        });
        
        raceControlPanel.add(startButton);
        raceControlPanel.add(resetButton);
        raceControlPanel.add(addHorseButton);
        
        controlPanel.add(raceControlPanel);
        
        // Add Statistics Panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        JButton quickStatsButton = new JButton("Quick Summary");
        quickStatsButton.addActionListener(e -> showQuickSummary());
        statsPanel.add(quickStatsButton);
            
        JButton analyzeButton = new JButton("Analyze Performance");
        analyzeButton.addActionListener(e -> analyzePerformanceTrends());
        statsPanel.add(analyzeButton);

        controlPanel.add(statsPanel);

        // Initialize the track with current settings
        updateTrack();
    }
    
    /**
     * Update the track based on current settings
     */
    private void updateTrack() {
        String trackType = (String)trackTypeCombo.getSelectedItem();
        int lanes = lanesSlider.getValue();
        int length = lengthSlider.getValue();
        TrackCondition condition = (TrackCondition)conditionCombo.getSelectedItem();
        
        Track newTrack;
        switch (trackType) {
            case "Figure-Eight":
                newTrack = new FigureEightTrack("Figure-Eight Track", length, lanes, condition);
                break;
            case "Zigzag":
                newTrack = new ZigzagTrack("Zigzag Track", length, lanes, condition);
                break;
            case "Oval":
            default:
                newTrack = new OvalTrack("Oval Track", length, lanes, condition);
                break;
        }
        
        raceManager.setTrack(newTrack);
        trackPanel.repaint();
        
        // Update the betting service with the new track
        if (!raceManager.isRaceInProgress()) {
            BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
        }
    }
    
    /**
     * Method to be called when a race is finished
     * This connects your race completion logic to the statistics system
     */
    private void onRaceFinished(Race race) {
        // Your existing race finished code...
        
        // Show a message that statistics were recorded
        JOptionPane.showMessageDialog(
            this,
            "Race completed and statistics recorded!\n" +
            "View them in the Statistics menu.",
            "Race Complete",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Add a new customized horse
     */
    private void addNewHorse() {
        if (raceManager.isRaceInProgress()) {
            JOptionPane.showMessageDialog(this, "Cannot add horses during a race",
                "Operation Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check for available lanes
        int maxLanes = raceManager.getTrack().getLanes();
        boolean anyLaneAvailable = false;
        
        for (int i = 0; i < maxLanes; i++) {
            boolean laneOccupied = false;
            for (Horse horse : raceManager.getHorses()) {
                if (raceManager.getLane(horse) == i) {
                    laneOccupied = true;
                    break;
                }
            }
            if (!laneOccupied) {
                anyLaneAvailable = true;
                break;
            }
        }
        
        if (!anyLaneAvailable) {
            JOptionPane.showMessageDialog(this, 
                "No lanes available. Please remove a horse or increase lane count.",
                "No Lanes Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Use the horse customizer to create a new horse
        Horse newHorse = HorseCustomizer.showDialog(this);
        
        if (newHorse != null) {
            // Find an available lane
            int lane = -1;
            for (int i = 0; i < maxLanes; i++) {
                boolean laneOccupied = false;
                for (Horse horse : raceManager.getHorses()) {
                    if (raceManager.getLane(horse) == i) {
                        laneOccupied = true;
                        break;
                    }
                }
                if (!laneOccupied) {
                    lane = i;
                    break;
                }
            }
            
            // Assign a color based on the horse's coat color
            Color horseColor = newHorse.getCoatColor().getColor();
            horseColors.put(newHorse, horseColor);
            
            // Add the horse to the race
            if (raceManager.addHorse(newHorse, lane)) {
                updateHorsePanel();
                // Update the betting service with the new horse
                if (!raceManager.isRaceInProgress()) {
                    BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to add horse to race", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Update the horse panel with current horses
     */
    private void updateHorsePanel() {
        horsePanel.removeAll();
        
        for (Horse horse : raceManager.getHorses()) {
            JPanel horseInfoPanel = new JPanel(new BorderLayout());
            horseInfoPanel.setBorder(BorderFactory.createTitledBorder(horse.getName()));
            
            // Create a panel for horse info in a grid layout
            JPanel infoGrid = new JPanel(new GridLayout(3, 2, 5, 2));
            
            // Add breed and color info
            infoGrid.add(new JLabel("Breed:"));
            infoGrid.add(new JLabel(horse.getBreed().getName()));
            infoGrid.add(new JLabel("Color:"));
            
            // Create a panel for color display
            JPanel colorPanel = new JPanel(new BorderLayout());
            JLabel colorLabel = new JLabel(horse.getCoatColor().getName());
            JPanel colorSwatch = new JPanel();
            colorSwatch.setBackground(horse.getCoatColor().getColor());
            colorSwatch.setPreferredSize(new Dimension(20, 15));
            colorSwatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            colorPanel.add(colorLabel, BorderLayout.CENTER);
            colorPanel.add(colorSwatch, BorderLayout.EAST);
            infoGrid.add(colorPanel);
            
            // Add confidence
            infoGrid.add(new JLabel("Confidence:"));
            infoGrid.add(new JLabel(String.format("%.2f", horse.getConfidence())));
            
            horseInfoPanel.add(infoGrid, BorderLayout.CENTER);
            
            // Create a panel for buttons and symbol
            JPanel controlsPanel = new JPanel(new BorderLayout());
            
            // Display symbol
            JPanel symbolPanel = new JPanel();
            symbolPanel.setBackground(horseColors.get(horse));
            JLabel symbolLabel = new JLabel(horse.getDisplaySymbol());
            symbolLabel.setFont(new Font("Dialog", Font.BOLD, 20));
            symbolLabel.setForeground(Color.WHITE);
            symbolPanel.add(symbolLabel);
            symbolPanel.setPreferredSize(new Dimension(40, 30));
            controlsPanel.add(symbolPanel, BorderLayout.WEST);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            // Edit button
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editHorse(horse);
                }
            });
            
            // Remove button
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeHorse(horse);
                }
            });
            
            buttonPanel.add(editButton);
            buttonPanel.add(removeButton);
            controlsPanel.add(buttonPanel, BorderLayout.CENTER);
            
            horseInfoPanel.add(controlsPanel, BorderLayout.SOUTH);
            
            // Add equipment info
            JPanel equipmentPanel = new JPanel();
            equipmentPanel.setLayout(new BoxLayout(equipmentPanel, BoxLayout.Y_AXIS));
            equipmentPanel.setBorder(BorderFactory.createTitledBorder("Equipment"));
            equipmentPanel.add(new JLabel("Saddle: " + horse.getEquipment().getSaddle().getName()));
            equipmentPanel.add(new JLabel("Horseshoes: " + horse.getEquipment().getHorseshoes().getName()));
            equipmentPanel.add(new JLabel("Accessory: " + horse.getEquipment().getAccessory().getName()));
            
            horseInfoPanel.add(equipmentPanel, BorderLayout.NORTH);
            
            // Add the horse panel to the main horse panel
            horsePanel.add(horseInfoPanel);
            horsePanel.add(Box.createVerticalStrut(10));
        }
        
        horsePanel.revalidate();
        horsePanel.repaint();
    }
    
    /**
     * Edit an existing horse
     * 
     * @param horse The horse to edit
     */
    private void editHorse(Horse horse) {
        if (raceManager.isRaceInProgress()) {
            JOptionPane.showMessageDialog(this, 
                "Cannot edit horses during a race", 
                "Operation Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Horse editedHorse = HorseCustomizer.showDialog(this, horse);
        if (editedHorse != null) {
            // Update the horse color
            horseColors.put(editedHorse, editedHorse.getCoatColor().getColor());
            updateHorsePanel();
            trackPanel.repaint();
            
            // Update the betting service after editing a horse
            if (!raceManager.isRaceInProgress()) {
                BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
            }
        }
    }
    
    /**
     * Remove a horse from the race
     * 
     * @param horse The horse to remove
     */
    private void removeHorse(Horse horse) {
        if (raceManager.isRaceInProgress()) {
            JOptionPane.showMessageDialog(this, 
                "Cannot remove horses during a race", 
                "Operation Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        raceManager.removeHorse(horse);
        horseColors.remove(horse);
        updateHorsePanel();
        trackPanel.repaint();
        
        // Update the betting service after removing a horse
        if (!raceManager.isRaceInProgress()) {
            BettingService.getInstance().startRace(raceManager.getHorses(), raceManager.getTrack());
        }
    }
    
    /**
     * Create some example horses for testing
     */
    private void createExampleHorses() {
        // Create horses with different breeds and equipment
        
        // Thoroughbred with racing equipment
        Horse horse1 = new Horse('A', "Thunderbolt", "🏇", 0.8, 
                                 HorseBreed.THOROUGHBRED, 
                                 CoatColor.BAY, 
                                 new HorseEquipment(
                                     HorseEquipment.SaddleType.RACING,
                                     HorseEquipment.HorseshoeType.LIGHTWEIGHT,
                                     HorseEquipment.AccessoryType.BLINDERS));
        
        // Arabian with balanced equipment
        Horse horse2 = new Horse('B', "Silver Wind", "🐎", 0.7, 
                                 HorseBreed.ARABIAN, 
                                 CoatColor.GRAY, 
                                 new HorseEquipment(
                                     HorseEquipment.SaddleType.ENGLISH,
                                     HorseEquipment.HorseshoeType.TRACTION,
                                     HorseEquipment.AccessoryType.LUCKY_CHARM));
        
        // Quarter Horse with strength equipment
        Horse horse3 = new Horse('C', "Dark Thunder", "♞", 0.85, 
                                 HorseBreed.QUARTER_HORSE, 
                                 CoatColor.BLACK, 
                                 new HorseEquipment(
                                     HorseEquipment.SaddleType.WESTERN,
                                     HorseEquipment.HorseshoeType.STANDARD,
                                     HorseEquipment.AccessoryType.PERFORMANCE_BRIDLE));
        
        // Add horses to the race manager
        raceManager.addHorse(horse1, 0);
        raceManager.addHorse(horse2, 1);
        raceManager.addHorse(horse3, 2);
        
        // Set colors based on coat colors
        horseColors.put(horse1, horse1.getCoatColor().getColor());
        horseColors.put(horse2, horse2.getCoatColor().getColor());
        horseColors.put(horse3, horse3.getCoatColor().getColor());
        
        // Update the horse panel
        updateHorsePanel();
    }
    
    /**
     * The panel that displays the race track and horses
     */
    private class RaceTrackPanel extends JPanel {
        
        public RaceTrackPanel() {
            setBackground(new Color(230, 230, 230));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Get the current track
            Track track = raceManager.getTrack();
            if (track == null) return;
            
            // Calculate the scale and offset to fit the track in the panel
            double maxX = 100, maxY = 100, minX = -100, minY = -100;
            List<Point2D.Double> trackPoints = track.getTrackPoints();
            
            // Find the bounding box of the track
            if (!trackPoints.isEmpty()) {
                for (Point2D.Double point : trackPoints) {
                    maxX = Math.max(maxX, point.x);
                    maxY = Math.max(maxY, point.y);
                    minX = Math.min(minX, point.x);
                    minY = Math.min(minY, point.y);
                }
            }
            
            // Add some padding
            maxX += 100; maxY += 100;
            minX -= 100; minY -= 100;
            
            double trackWidth = maxX - minX;
            double trackHeight = maxY - minY;
            
            // Calculate the scale to fit the track in the panel
            double scaleX = getWidth() / trackWidth;
            double scaleY = getHeight() / trackHeight;
            double scale = Math.min(scaleX, scaleY) * 0.9;
            
            // Calculate the offset to center the track
            double offsetX = (getWidth() - (trackWidth * scale)) / 2 - minX * scale;
            double offsetY = (getHeight() - (trackHeight * scale)) / 2 - minY * scale;
            
            // Draw the track outline
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.BLACK);
            
            // Draw track based on its type
            if (track instanceof OvalTrack) {
                // Draw an oval
                int ovalWidth = (int)(trackWidth * scale * 0.8);
                int ovalHeight = (int)(trackHeight * scale * 0.8);
                int x = (getWidth() - ovalWidth) / 2;
                int y = (getHeight() - ovalHeight) / 2;
                g2d.drawOval(x, y, ovalWidth, ovalHeight);
            } else if (track instanceof FigureEightTrack) {
                // Draw a figure-8
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = (int)(Math.min(getWidth(), getHeight()) * 0.3);
                
                // Draw two circles that overlap to form a figure-8
                g2d.drawOval(centerX - radius, centerY - radius/2 - radius, radius*2, radius*2);
                g2d.drawOval(centerX - radius, centerY + radius/2 - radius, radius*2, radius*2);
            } else if (track instanceof ZigzagTrack) {
                // Draw a zigzag track
                int startX = getWidth() / 5;
                int endX = getWidth() * 4 / 5;
                int topY = getHeight() / 5;
                int bottomY = getHeight() * 4 / 5;
                int segments = 6;
                int segmentWidth = (endX - startX) / segments;
                
                int x = startX;
                int y = topY;
                
                for (int i = 0; i <= segments; i++) {
                    int nextX = startX + i * segmentWidth;
                    int nextY = (i % 2 == 0) ? bottomY : topY;
                    
                    g2d.drawLine(x, y, nextX, nextY);
                    x = nextX;
                    y = nextY;
                }
            } else if (!trackPoints.isEmpty()) {
                // Draw the track using the track points
                for (int i = 0; i < trackPoints.size(); i++) {
                    Point2D.Double p1 = trackPoints.get(i);
                    Point2D.Double p2 = trackPoints.get((i + 1) % trackPoints.size());
                    
                    int x1 = (int) (p1.x * scale + offsetX);
                    int y1 = (int) (p1.y * scale + offsetY);
                    int x2 = (int) (p2.x * scale + offsetX);
                    int y2 = (int) (p2.y * scale + offsetY);
                    
                    g2d.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
            
            // Draw the track condition indicator
            String conditionText = "Track Condition: " + track.getCondition().getName();
            g2d.setColor(Color.BLACK);
            g2d.drawString(conditionText, 10, 20);
            
            // Draw the horses
            for (Horse horse : raceManager.getHorses()) {
                int lane = raceManager.getLane(horse);
                Point2D.Double position;
                
                // Calculate position based on track type
                if (track instanceof OvalTrack) {
                    double angle = (horse.getDistanceTravelled() / track.getLength()) * 2 * Math.PI;
                    int ovalWidth = (int)(trackWidth * scale * 0.8);
                    int ovalHeight = (int)(trackHeight * scale * 0.8);
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int radius = lane * 10; // Space between lanes
                    
                    int x = centerX + (int)((ovalWidth/2 + radius) * Math.cos(angle - Math.PI/2));
                    int y = centerY + (int)((ovalHeight/2 + radius) * Math.sin(angle - Math.PI/2));
                    position = new Point2D.Double(x, y);
                } else if (track instanceof FigureEightTrack) {
                    double t = (horse.getDistanceTravelled() / track.getLength()) * 2 * Math.PI;
                    int centerX = getWidth() / 2;
                    int centerY = getHeight() / 2;
                    int radius = (int)(Math.min(getWidth(), getHeight()) * 0.3);
                    int laneOffset = lane * 10; // Space between lanes
                    
                    int x = centerX + (int)((radius + laneOffset) * Math.sin(t));
                    int y = centerY + (int)((radius/2 + laneOffset) * Math.sin(2 * t));
                    position = new Point2D.Double(x, y);
                } else if (track instanceof ZigzagTrack) {
                    int startX = getWidth() / 5;
                    int endX = getWidth() * 4 / 5;
                    int topY = getHeight() / 5;
                    int bottomY = getHeight() * 4 / 5;
                    int segments = 6;
                    int segmentWidth = (endX - startX) / segments;
                    double progress = horse.getDistanceTravelled() / track.getLength();
                    int segment = (int)(progress * segments);
                    double segmentProgress = (progress * segments) - segment;
                    
                    int x, y;
                    if (segment % 2 == 0) {
                        x = startX + segment * segmentWidth + (int)(segmentProgress * segmentWidth);
                        y = topY + (int)(segmentProgress * (bottomY - topY)) + lane * 10;
                    } else {
                        x = startX + segment * segmentWidth + (int)(segmentProgress * segmentWidth);
                        y = bottomY - (int)(segmentProgress * (bottomY - topY)) + lane * 10;
                    }
                    position = new Point2D.Double(x, y);
                } else {
                    position = track.calculatePosition(horse.getDistanceTravelled(), lane);
                    position.x = position.x * scale + offsetX;
                    position.y = position.y * scale + offsetY;
                }
                
                int x = (int) position.x;
                int y = (int) position.y;
                
                // Draw a colored circle for the horse
                g2d.setColor(horseColors.get(horse));
                if (horse.hasFallen()) {
                    // Draw an X for fallen horses
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(x - 10, y - 10, x + 10, y + 10);
                    g2d.drawLine(x + 10, y - 10, x - 10, y + 10);
                } else {
                    g2d.fill(new Ellipse2D.Double(x - 15, y - 15, 30, 30));
                    
                    // Draw the horse symbol in the center
                    g2d.setColor(Color.WHITE);
                    String symbol = horse.getDisplaySymbol();
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x - (fm.stringWidth(symbol) / 2);
                    int textY = y + (fm.getHeight() / 4);
                    g2d.drawString(symbol, textX, textY);
                }
                
                // Draw the horse name above the horse
                g2d.setColor(Color.BLACK);
                String nameText = horse.getName();
                FontMetrics fm = g2d.getFontMetrics();
                int textX = x - (fm.stringWidth(nameText) / 2);
                int textY = y - 20;
                g2d.drawString(nameText, textX, textY);
                
                // Draw breed and progress below the horse
                String breedText = horse.getBreed().getName();
                String progressText = String.format("%.0f%%", (horse.getDistanceTravelled() / track.getLength()) * 100);
                String infoText = breedText + " - " + progressText;
                textX = x - (fm.stringWidth(infoText) / 2);
                textY = y + 30;
                g2d.drawString(infoText, textX, textY);
                
                // Display odds if not in a race
                if (!raceManager.isRaceInProgress()) {
                    double odds = BettingService.getInstance().getOddsForHorse(horse);
                    if (odds > 0) {
                        String oddsText = String.format("Odds: %.1f:1", odds);
                        textX = x - (fm.stringWidth(oddsText) / 2);
                        textY = y + 45;
                        g2d.drawString(oddsText, textX, textY);
                    }
                }
            }
            
            // Draw the winner declaration if a race has finished
            Horse winner = raceManager.getWinner();
            if (winner != null && !raceManager.isRaceInProgress()) {
                g2d.setColor(new Color(0, 100, 0));
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String winnerText = winner.getName() + " (" + winner.getBreed().getName() + ") wins!";
                g2d.drawString(winnerText, (getWidth() - g2d.getFontMetrics().stringWidth(winnerText)) / 2, 50);
                
                // If there were any winning bets, show that too
                BettingHistory history = BettingService.getInstance().getBettingHistory();
                List<Bet> winningBets = history.getWinningBets();
                if (!winningBets.isEmpty()) {
                    // Find winning bets on this horse
                    double totalPayout = 0;
                    for (Bet bet : winningBets) {
                        if (bet.getHorse().equals(winner)) {
                            totalPayout += bet.getPayout();
                        }
                    }
                    
                    if (totalPayout > 0) {
                        DecimalFormat format = new DecimalFormat("$#,##0.00");
                        String payoutText = "Total payout: " + format.format(totalPayout);
                        g2d.setFont(new Font("Arial", Font.BOLD, 16));
                        g2d.setColor(new Color(0, 100, 0));
                        g2d.drawString(payoutText, (getWidth() - g2d.getFontMetrics().stringWidth(payoutText)) / 2, 75);
                    }
                }
            }
        }
    }
    
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        // Set look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RacingGUI();
            }
        });
    }
}