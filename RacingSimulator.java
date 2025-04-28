import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

/**
 * Main class for the Horse Racing Simulator
 * Provides a central entry point for both console and GUI modes
 * Now includes betting system integration
 * 
 * @author (Your Name)
 * @version (2.0)
 */
public class RacingSimulator {
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        // Display welcome message
        System.out.println("=================================");
        System.out.println("  HORSE RACING SIMULATOR v2.0");
        System.out.println("=================================");
        
        // Initialize the betting system
        initializeBettingSystem();
        
        // Check for command-line arguments
        boolean forceConsole = false;
        boolean forceGUI = false;
        
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--console")) {
                forceConsole = true;
            } else if (arg.equalsIgnoreCase("--gui")) {
                forceGUI = true;
            }
        }
        
        // If conflicting options, prioritize console mode
        if (forceConsole && forceGUI) {
            forceGUI = false;
            System.out.println("Conflicting options. Defaulting to console mode.");
        }
        
        // If forcing a specific mode, use it
        if (forceConsole) {
            runConsoleMode();
            return;
        } else if (forceGUI) {
            runGUIMode();
            return;
        }
        
        // Otherwise, ask the user for their preferred mode
        System.out.println("\nPlease select a mode:");
        System.out.println("1. Console Mode (text-based)");
        System.out.println("2. GUI Mode (graphical interface)");
        System.out.print("Enter your choice (1-2): ");
        
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        int choice = 0;
        
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to GUI Mode.");
            choice = 2;
        }
        
        if (choice == 1) {
            runConsoleMode();
        } else {
            runGUIMode();
        }
    }
    
    /**
     * Initialize the betting system components
     */
    private static void initializeBettingSystem() {
        // Get the singleton instance to ensure it's initialized
        BettingService bettingService = BettingService.getInstance();
        System.out.println("Betting system initialized with $" + 
            bettingService.getWallet().getBalance() + " in virtual wallet");
    }
    
    /**
     * Run the application in console mode
     * Now includes betting functionality
     */
    private static void runConsoleMode() {
        System.out.println("\nStarting Console Mode...");
        
        // Create a new race with default settings
        Race race = new Race(50);
        BettingService bettingService = BettingService.getInstance();
        
        // Menu system for console mode
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n=== CONSOLE RACE MENU ===");
            System.out.println("1. Add Horses");
            System.out.println("2. Change Track");
            System.out.println("3. Place Bet");
            System.out.println("4. View Wallet Balance");
            System.out.println("5. View Betting History");
            System.out.println("6. Start Race");
            System.out.println("7. Exit");
            System.out.print("Enter your choice (1-7): ");
            
            int choice = 0;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (Exception e) {
                scanner.nextLine(); // Consume invalid input
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            
            switch (choice) {
                case 1:
                    addHorsesConsole(race, scanner);
                    break;
                case 2:
                    changeTrackConsole(race, scanner);
                    break;
                case 3:
                    placeBetConsole(race, scanner, bettingService);
                    break;
                case 4:
                    System.out.println("\n=== WALLET BALANCE ===");
                    System.out.printf("Current balance: $%.2f%n", 
                        bettingService.getWallet().getBalance());
                    break;
                case 5:
                    viewBettingHistoryConsole(bettingService);
                    break;
                case 6:
                    if (race.getHorses().isEmpty()) {
                        System.out.println("Please add horses first!");
                    } else {
                        System.out.println("\nStarting the race...");
                        race.startRace();
                    }
                    break;
                case 7:
                    exit = true;
                    System.out.println("Thank you for using Horse Racing Simulator!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    /**
     * Place a bet in console mode
     */
    private static void placeBetConsole(Race race, java.util.Scanner scanner, BettingService bettingService) {
        if (bettingService.isRaceInProgress()) {
            System.out.println("Cannot place bets while a race is in progress");
            return;
        }
        
        System.out.println("\n=== PLACE BET ===");
        
        // Show current balance
        double balance = bettingService.getWallet().getBalance();
        System.out.printf("Current balance: $%.2f%n", balance);
        
        if (balance <= 0) {
            System.out.println("Insufficient funds to place a bet.");
            return;
        }
        
        // Show available horses and their odds
        List<Horse> horses = race.getHorses();
        if (horses.isEmpty()) {
            System.out.println("No horses available to bet on. Please add horses first.");
            return;
        }
        
        System.out.println("\nAvailable horses and odds:");
        Map<Horse, Double> odds = bettingService.getCurrentOdds();
        
        // If no odds are calculated yet, initialize them
        if (odds.isEmpty()) {
            bettingService.startRace(horses, race.getTrack());
            odds = bettingService.getCurrentOdds();
        }
        
        for (int i = 0; i < horses.size(); i++) {
            Horse horse = horses.get(i);
            double horseOdds = odds.getOrDefault(horse, 2.0);
            System.out.printf("%d. %s (%.1f:1)%n", i+1, horse.getName(), horseOdds);
        }
        
        // Select a horse
        System.out.print("\nSelect horse number: ");
        int horseIndex = 0;
        try {
            horseIndex = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (horseIndex < 1 || horseIndex > horses.size()) {
                System.out.println("Invalid horse number. Cancelling bet.");
                return;
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Cancelling bet.");
            return;
        }
        
        Horse selectedHorse = horses.get(horseIndex - 1);
        
        // Enter bet amount
        System.out.printf("Enter bet amount (max $%.2f): ", balance);
        double betAmount = 0;
        try {
            betAmount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
            
            if (betAmount <= 0 || betAmount > balance) {
                System.out.println("Invalid bet amount. Cancelling bet.");
                return;
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Cancelling bet.");
            return;
        }
        
        // Confirm the bet
        double potentialPayout = betAmount * odds.getOrDefault(selectedHorse, 2.0);
        System.out.printf("\nConfirm bet of $%.2f on %s at %.1f:1", 
            betAmount, selectedHorse.getName(), odds.getOrDefault(selectedHorse, 2.0));
        System.out.printf("\nPotential payout: $%.2f", potentialPayout);
        System.out.print("\nConfirm? (y/n): ");
        
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.startsWith("y")) {
            System.out.println("Bet cancelled.");
            return;
        }
        
        // Place the bet
        try {
            Bet bet = bettingService.placeBet(selectedHorse, betAmount);
            System.out.println("Bet placed successfully!");
            System.out.printf("New balance: $%.2f%n", bettingService.getWallet().getBalance());
        } catch (VirtualWallet.InsufficientFundsException e) {
            System.out.println("Insufficient funds for this bet.");
        } catch (Exception e) {
            System.out.println("Error placing bet: " + e.getMessage());
        }
    }
    
    /**
     * View betting history in console mode
     */
    private static void viewBettingHistoryConsole(BettingService bettingService) {
        System.out.println("\n=== BETTING HISTORY ===");
        
        BettingHistory history = bettingService.getBettingHistory();
        List<Bet> bets = history.getAllBets();
        
        if (bets.isEmpty()) {
            System.out.println("No bets have been placed yet.");
            return;
        }
        
        System.out.println("Recent bets:");
        for (int i = bets.size() - 1; i >= Math.max(0, bets.size() - 10); i--) {
            Bet bet = bets.get(i);
            String status = bet.isSettled() 
                ? (bet.isWon() ? "Won" : "Lost") 
                : "Pending";
            String payout = bet.isWon() 
                ? String.format("$%.2f", bet.getPayout()) 
                : "-";
                
            System.out.printf("%s: $%.2f on %s at %.1f:1 [%s] Payout: %s%n",
                bet.getTimestamp(), bet.getAmount(), bet.getHorse().getName(),
                bet.getOdds(), status, payout);
        }
        
        // Summary statistics
        double totalBetAmount = history.getTotalBetAmount();
        double totalWinnings = history.getTotalWinnings();
        double netProfit = totalWinnings - totalBetAmount;
        
        System.out.printf("\nSummary:%n");
        System.out.printf("Total bet amount: $%.2f%n", totalBetAmount);
        System.out.printf("Total winnings: $%.2f%n", totalWinnings);
        System.out.printf("Net profit: $%.2f%n", netProfit);
        System.out.printf("Current balance: $%.2f%n", bettingService.getWallet().getBalance());
    }
    
    /**
     * Helper method to add horses in console mode
     */
    private static void addHorsesConsole(Race race, java.util.Scanner scanner) {
        System.out.println("\n=== ADD HORSES ===");
        
        // Show existing horses
        List<Horse> horses = race.getHorses();
        if (!horses.isEmpty()) {
            System.out.println("Current horses:");
            for (int i = 0; i < horses.size(); i++) {
                Horse horse = horses.get(i);
                int lane = race.getLaneAssignments().get(i);
                System.out.printf("%d. %s (Lane %d, Confidence %.2f)%n", 
                    i+1, horse.getName(), lane, horse.getConfidence());
            }
            System.out.println();
        }
        
        // Add a new horse
        System.out.print("Enter horse name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter horse symbol (single character): ");
        char symbol = scanner.nextLine().charAt(0);
        
        System.out.print("Enter confidence (0.0-1.0): ");
        double confidence = 0.7;
        try {
            confidence = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Using default confidence 0.7.");
        }
        
        // Check valid confidence range
        if (confidence < 0.0 || confidence > 1.0) {
            System.out.println("Confidence must be between 0.0 and 1.0. Using 0.7.");
            confidence = 0.7;
        }
        
        // Get available lanes
        List<Integer> occupiedLanes = race.getLaneAssignments();
        int maxLanes = race.getTrack().getLanes();
        
        System.out.println("Available lanes:");
        boolean anyLaneAvailable = false;
        
        for (int i = 0; i < maxLanes; i++) {
            if (!occupiedLanes.contains(i)) {
                System.out.print(i + " ");
                anyLaneAvailable = true;
            }
        }
        
        if (!anyLaneAvailable) {
            System.out.println("No lanes available. Please remove a horse or increase lane count.");
            return;
        }
        
        System.out.print("\nEnter lane number: ");
        int lane = 0;
        try {
            lane = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Cancelling horse addition.");
            return;
        }
        
        // Create and add the horse
        Horse newHorse = new Horse(symbol, name, confidence);
        if (race.addHorse(newHorse, lane)) {
            System.out.println("Horse added successfully!");
            
            // Update the betting service with the new horse list
            BettingService.getInstance().startRace(race.getHorses(), race.getTrack());
        } else {
            System.out.println("Failed to add horse. Lane might be invalid or occupied.");
        }
    }
    
    /**
     * Helper method to change track settings in console mode
     */
    private static void changeTrackConsole(Race race, java.util.Scanner scanner) {
        System.out.println("\n=== CHANGE TRACK ===");
        System.out.println("1. Oval Track");
        System.out.println("2. Figure-Eight Track");
        System.out.println("3. Zigzag Track");
        System.out.print("Select track type (1-3): ");
        
        int trackType = 1;
        try {
            trackType = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Using default Oval track.");
        }
        
        System.out.print("Enter track length (100-1000): ");
        int length = 500;
        try {
            length = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (length < 100 || length > 1000) {
                System.out.println("Length must be between 100 and 1000. Using 500.");
                length = 500;
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Using default length 500.");
        }
        
        System.out.print("Enter number of lanes (2-8): ");
        int lanes = 5;
        try {
            lanes = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (lanes < 2 || lanes > 8) {
                System.out.println("Lanes must be between 2 and 8. Using 5.");
                lanes = 5;
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Using default 5 lanes.");
        }
        
        System.out.println("Track conditions:");
        System.out.println("1. Dry");
        System.out.println("2. Muddy");
        System.out.println("3. Icy");
        System.out.println("4. Wet");
        System.out.println("5. Windy");
        System.out.print("Select condition (1-5): ");
        
        int conditionChoice = 1;
        try {
            conditionChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (conditionChoice < 1 || conditionChoice > 5) {
                System.out.println("Invalid choice. Using Dry condition.");
                conditionChoice = 1;
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Invalid input. Using default Dry condition.");
        }
        
        // Get the selected track condition
        TrackCondition condition;
        switch (conditionChoice) {
            case 2: condition = TrackCondition.MUDDY; break;
            case 3: condition = TrackCondition.ICY; break;
            case 4: condition = TrackCondition.WET; break;
            case 5: condition = TrackCondition.WINDY; break;
            default: condition = TrackCondition.DRY;
        }
        
        // Create the appropriate track
        Track newTrack;
        switch (trackType) {
            case 2:
                newTrack = new FigureEightTrack("Figure-Eight Track", length, lanes, condition);
                break;
            case 3:
                newTrack = new ZigzagTrack("Zigzag Track", length, lanes, condition);
                break;
            default:
                newTrack = new OvalTrack("Oval Track", length, lanes, condition);
        }
        
        // Set the new track
        race.setTrack(newTrack);
        System.out.println("Track updated successfully!");
        
        // Update the betting service with the new track
        BettingService.getInstance().startRace(race.getHorses(), race.getTrack());
        
        // Check if any horses need to be removed due to lane reduction
        List<Horse> horses = race.getHorses();
        List<Integer> laneAssignments = race.getLaneAssignments();
        
        for (int i = horses.size() - 1; i >= 0; i--) {
            if (laneAssignments.get(i) >= lanes) {
                System.out.println("Removing " + horses.get(i).getName() + 
                    " as its lane no longer exists.");
                // Note: In a real implementation, you would need a method to remove horses by index
                // For now, we're assuming the Race class has appropriate methods
            }
        }
    }
    
    /**
     * Run the application in GUI mode
     */
    private static void runGUIMode() {
        System.out.println("\nStarting GUI Mode...");
        
        // Set system look and feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not set system look and feel. Using default.");
        }
        
        // Launch the GUI on the event dispatch thread
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // In the runGUIMode() method or wherever RacingGUI is instantiated
                RacingGUI gui = new RacingGUI();
                System.out.println("GUI created. Menu bar exists: " + (gui.getJMenuBar() != null));
                System.out.println("Menu count: " + (gui.getJMenuBar() != null ? gui.getJMenuBar().getMenuCount() : 0));
            }
        });
    }
}