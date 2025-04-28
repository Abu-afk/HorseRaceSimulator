import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * The main UI panel for the betting system.
 * Provides odds display, bet placement, trends, and balance tracking.
 */
public class BettingPanel extends JPanel {
    // Services
    private BettingService bettingService;
    private RaceManager raceManager;
    
    // UI Components
    private JLabel balanceLabel;
    private JTable oddsTable;
    private DefaultTableModel oddsTableModel;
    private JComboBox<Horse> horseSelector;
    private JTextField betAmountField;
    private JButton placeBetButton;
    private JPanel betEntryPanel;
    private JPanel oddsPanel;
    private JPanel trendsPanel;
    private JLabel feedbackLabel;
    
    // Formatters
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private DecimalFormat oddsFormat = new DecimalFormat("#0.0");
    private DecimalFormat percentFormat = new DecimalFormat("0.0%");
    
    /**
     * Constructor for BettingPanel
     */
    public BettingPanel() {
        this.bettingService = BettingService.getInstance();
        this.raceManager = RaceManagerSingleton.getInstance();
        
        setupUI();
        setupEventListeners();
        refreshUI();
    }
    
    /**
     * Set up the UI components
     */
    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Balance display at the top
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        balancePanel.setBorder(BorderFactory.createTitledBorder("Virtual Wallet"));
        balanceLabel = new JLabel("Balance: $0.00");
        balanceLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        balancePanel.add(balanceLabel);
        
        add(balancePanel, BorderLayout.NORTH);
        
        // Main content panel with odds and betting
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        
        // Odds panel on the left
        setupOddsPanel();
        contentPanel.add(oddsPanel, BorderLayout.WEST);
        
        // Betting control panel on the right
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        
        // Bet entry panel
        setupBetEntryPanel();
        rightPanel.add(betEntryPanel, BorderLayout.NORTH);
        
        // Trends panel
        setupTrendsPanel();
        rightPanel.add(trendsPanel, BorderLayout.CENTER);
        
        contentPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Feedback label at the bottom
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(feedbackLabel, BorderLayout.SOUTH);
    }
    
    /**
     * Set up the odds display panel
     */
    private void setupOddsPanel() {
        oddsPanel = new JPanel(new BorderLayout(5, 5));
        oddsPanel.setBorder(BorderFactory.createTitledBorder("Current Odds"));
        
        // Create table model with column names
        String[] columnNames = {"Horse", "Odds", "Total Bets"};
        oddsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Create table
        oddsTable = new JTable(oddsTableModel);
        oddsTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        oddsTable.setFillsViewportHeight(true);
        oddsTable.getTableHeader().setReorderingAllowed(false);
        
        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(oddsTable);
        oddsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Odds");
        refreshButton.addActionListener(e -> updateOddsTable());
        oddsPanel.add(refreshButton, BorderLayout.SOUTH);
    }
    
    /**
     * Set up the bet entry panel
     */
    private void setupBetEntryPanel() {
        betEntryPanel = new JPanel(new GridBagLayout());
        betEntryPanel.setBorder(BorderFactory.createTitledBorder("Place Bet"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Horse selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        betEntryPanel.add(new JLabel("Horse:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        horseSelector = new JComboBox<>();
        updateHorseSelector();
        betEntryPanel.add(horseSelector, gbc);
        
        // Bet amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        betEntryPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        betAmountField = new JTextField("10.00");
        betEntryPanel.add(betAmountField, gbc);
        
        // Place bet button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        placeBetButton = new JButton("Place Bet");
        placeBetButton.addActionListener(e -> placeBet());
        betEntryPanel.add(placeBetButton, gbc);
    }
    
    /**
     * Set up the trends and statistics panel
     */
    private void setupTrendsPanel() {
        trendsPanel = new JPanel(new BorderLayout(5, 5));
        trendsPanel.setBorder(BorderFactory.createTitledBorder("Betting Statistics"));
        
        // Create table model with column names
        String[] columnNames = {"Horse", "Bets", "Wins", "Losses", "Win Rate"};
        DefaultTableModel trendsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Create table
        JTable trendsTable = new JTable(trendsTableModel);
        trendsTable.setPreferredScrollableViewportSize(new Dimension(300, 150));
        trendsTable.setFillsViewportHeight(true);
        trendsTable.getTableHeader().setReorderingAllowed(false);
        
        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(trendsTable);
        trendsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add overall statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Overall Stats"));
        
        statsPanel.add(new JLabel("Total bets: 0"));
        statsPanel.add(new JLabel("Win rate: 0.0%"));
        statsPanel.add(new JLabel("Net profit: $0.00"));
        
        trendsPanel.add(statsPanel, BorderLayout.SOUTH);
        
        // Update trends table
        updateTrendsTable(trendsTableModel);
    }
    
    /**
     * Set up event listeners for the betting service
     */
    private void setupEventListeners() {
        // Add betting service listener
        bettingService.addBettingServiceListener(new BettingService.BettingServiceListener() {
            @Override
            public void oddsChanged(BettingService.BettingServiceEvent event) {
                SwingUtilities.invokeLater(() -> {
                    updateOddsTable();
                });
            }
            
            @Override
            public void betPlaced(BettingService.BettingServiceEvent event) {
                SwingUtilities.invokeLater(() -> {
                    Bet bet = event.getBet();
                    refreshUI();
                    feedbackLabel.setText("Bet placed: " + currencyFormat.format(bet.getAmount()) + 
                                         " on " + bet.getHorse().getName() + " at " + 
                                         oddsFormat.format(bet.getOdds()) + ":1");
                });
            }
            
            @Override
            public void raceSettled(BettingService.BettingServiceEvent event) {
                SwingUtilities.invokeLater(() -> {
                    Horse winner = event.getWinningHorse();
                    double payout = event.getPayout();
                    refreshUI();
                    if (payout > 0) {
                        feedbackLabel.setText("Race settled: " + winner.getName() + 
                                             " won! You won " + currencyFormat.format(payout));
                    } else {
                        feedbackLabel.setText("Race settled: " + winner.getName() + 
                                             " won! Better luck next time.");
                    }
                });
            }
            
            @Override
            public void raceEnded(BettingService.BettingServiceEvent event) {
                SwingUtilities.invokeLater(() -> {
                    refreshUI();
                    feedbackLabel.setText("Race ended.");
                });
            }
            
            @Override
            public void bettingSystemReset(BettingService.BettingServiceEvent event) {
                SwingUtilities.invokeLater(() -> {
                    refreshUI();
                    feedbackLabel.setText("Betting system reset.");
                });
            }
        });
        
        if (raceManager != null) {
            // Add race manager listener
            raceManager.addRaceListener(new RaceManager.RaceListener() {
                @Override
                public void onRaceStart() {
                    SwingUtilities.invokeLater(() -> {
                        placeBetButton.setEnabled(false);
                        feedbackLabel.setText("Race in progress. No more bets allowed.");
                    });
                }
                
                @Override
                public void onHorseFallen(Horse horse) {
                    // Not needed for betting UI
                }
                
                @Override
                public void onRaceWinner(Horse horse) {
                    // Winner will be handled in raceSettled
                }
                
                @Override
                public void onRaceUpdate() {
                    // Not needed for betting UI
                }
                
                @Override
                public void onRaceEnd(Horse winner) {
                    SwingUtilities.invokeLater(() -> {
                        if (winner != null) {
                            // Settle bets and update UI
                            try {
                                bettingService.settleRace(winner);
                            } catch (Exception ex) {
                                System.err.println("Error settling race: " + ex.getMessage());
                            }
                        } else {
                            // Race ended without a winner
                            bettingService.endRace();
                        }
                        
                        placeBetButton.setEnabled(true);
                    });
                }
            });
        }
    }
    
    /**
     * Update all UI components
     */
    public void refreshUI() {
        updateBalanceLabel();
        updateOddsTable();
        updateHorseSelector();
        updateTrendsPanel();
    }
    
    /**
     * Update the balance label
     */
    private void updateBalanceLabel() {
        double balance = bettingService.getWallet().getBalance();
        balanceLabel.setText("Balance: " + currencyFormat.format(balance));
    }
    
    /**
     * Update the odds table
     */
    private void updateOddsTable() {
        // Clear the table
        oddsTableModel.setRowCount(0);
        
        // Get current odds and bet counts
        Map<Horse, Double> odds = bettingService.getCurrentOdds();
        BettingHistory history = bettingService.getBettingHistory();
        Map<Horse, Integer> betCounts = history.getBetCountByHorse();
        
        // Add rows for each horse
        for (Horse horse : odds.keySet()) {
            double horseOdds = odds.get(horse);
            int betCount = betCounts.getOrDefault(horse, 0);
            
            Object[] row = {
                horse.getName(),
                oddsFormat.format(horseOdds) + ":1",
                betCount
            };
            
            oddsTableModel.addRow(row);
        }
    }
    
    /**
     * Update the horse selector
     */
    private void updateHorseSelector() {
        horseSelector.removeAllItems();
        
        // Get horses from the race manager
        List<Horse> horses = raceManager != null ? raceManager.getHorses() : null;
        
        // Get current odds to ensure we only show horses in the current race
        Map<Horse, Double> odds = bettingService.getCurrentOdds();
        
        // Add horses to the selector
        if (odds.isEmpty() && horses != null) {
            // If no odds calculated yet, use all horses from race manager
            for (Horse horse : horses) {
                horseSelector.addItem(horse);
            }
        } else {
            // Otherwise use horses with calculated odds
            for (Horse horse : odds.keySet()) {
                horseSelector.addItem(horse);
            }
        }
        
        // Disable if race in progress
        horseSelector.setEnabled(!bettingService.isRaceInProgress());
    }
    
    /**
     * Update the trends panel
     */
    private void updateTrendsPanel() {
        // Find the JTable in the trends panel
        JTable trendsTable = null;
        for (Component comp : trendsPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                if (scrollPane.getViewport().getView() instanceof JTable) {
                    trendsTable = (JTable) scrollPane.getViewport().getView();
                    break;
                }
            }
        }
        
        if (trendsTable == null) {
            return;
        }
        
        // Update trends table
        DefaultTableModel trendsTableModel = (DefaultTableModel) trendsTable.getModel();
        updateTrendsTable(trendsTableModel);
        
        // Find the stats panel
        JPanel statsPanel = null;
        for (Component comp : trendsPanel.getComponents()) {
            if (comp instanceof JPanel && ((JPanel) comp).getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder) ((JPanel) comp).getBorder();
                if (border.getTitle().equals("Overall Stats")) {
                    statsPanel = (JPanel) comp;
                    break;
                }
            }
        }
        
        if (statsPanel == null) {
            return;
        }
        
        // Update stats panel
        BettingHistory history = bettingService.getBettingHistory();
        int totalBets = history.getTotalBetCount();
        double winRate = history.getOverallWinRate();
        double netProfit = history.getTotalWinnings() - history.getTotalBetAmount();
        
        // Find labels in stats panel
        if (statsPanel.getComponentCount() >= 3) {
            ((JLabel) statsPanel.getComponent(0)).setText("Total bets: " + totalBets);
            ((JLabel) statsPanel.getComponent(1)).setText("Win rate: " + percentFormat.format(winRate));
            ((JLabel) statsPanel.getComponent(2)).setText("Net profit: " + currencyFormat.format(netProfit));
        }
    }
    
    /**
     * Update the trends table
     */
    private void updateTrendsTable(DefaultTableModel model) {
        // Clear the table
        model.setRowCount(0);
        
        // Get betting history
        BettingHistory history = bettingService.getBettingHistory();
        Map<Horse, Integer> betCounts = history.getBetCountByHorse();
        Map<Horse, Integer> winCounts = history.getWinCountByHorse();
        Map<Horse, Integer> lossCounts = history.getLossCountByHorse();
        
        // Add rows for each horse
        for (Horse horse : betCounts.keySet()) {
            int bets = betCounts.get(horse);
            int wins = winCounts.getOrDefault(horse, 0);
            int losses = lossCounts.getOrDefault(horse, 0);
            double winRate = bets > 0 ? (double) wins / bets : 0.0;
            
            Object[] row = {
                horse.getName(),
                bets,
                wins,
                losses,
                percentFormat.format(winRate)
            };
            
            model.addRow(row);
        }
    }
    
    /**
     * Place a bet with the entered amount on the selected horse
     */
    private void placeBet() {
        // Get selected horse
        Horse selectedHorse = (Horse) horseSelector.getSelectedItem();
        if (selectedHorse == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a horse to bet on.", 
                "No Horse Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Parse bet amount
        double amount;
        try {
            amount = Double.parseDouble(betAmountField.getText());
            if (amount <= 0) {
                throw new NumberFormatException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid positive number for the bet amount.", 
                "Invalid Amount", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get current odds for the horse
        double odds = bettingService.getOddsForHorse(selectedHorse);
        double potentialPayout = amount * odds;
        
        // Confirm the bet
        int result = JOptionPane.showConfirmDialog(this,
            "Confirm bet of " + currencyFormat.format(amount) + 
            " on " + selectedHorse.getName() + " at " + oddsFormat.format(odds) + ":1\n" +
            "Potential payout: " + currencyFormat.format(potentialPayout),
            "Confirm Bet",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // Place the bet
            try {
                bettingService.placeBet(selectedHorse, amount);
                // UI will be updated via the event listener
            } catch (VirtualWallet.InsufficientFundsException e) {
                JOptionPane.showMessageDialog(this, 
                    "You don't have enough funds for this bet.", 
                    "Insufficient Funds", 
                    JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error placing bet: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}