import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;
import java.util.EventListener;

/**
 * Main service class for the betting system.
 * Manages odds calculation, bet placement, and race settlement.
 * Provides events for UI components to stay updated.
 */
public class BettingService {
    private static BettingService instance;
    
    private OddsCalculator oddsCalculator;
    private BettingHistory bettingHistory;
    private VirtualWallet wallet;
    private Map<Horse, Double> currentOdds;
    private EventListenerList listenerList;
    private boolean raceInProgress;
    
    /**
     * Private constructor for singleton pattern
     */
    private BettingService() {
        this.oddsCalculator = new OddsCalculator();
        this.bettingHistory = new BettingHistory();
        this.wallet = new VirtualWallet();
        this.currentOdds = new HashMap<>();
        this.listenerList = new EventListenerList();
        this.raceInProgress = false;
    }
    
    /**
     * Get the singleton instance
     * 
     * @return The BettingService instance
     */
    public static synchronized BettingService getInstance() {
        if (instance == null) {
            instance = new BettingService();
        }
        return instance;
    }
    
    /**
     * Get the current odds for all horses
     * 
     * @return Map of horse to odds
     */
    public Map<Horse, Double> getCurrentOdds() {
        return new HashMap<>(currentOdds);
    }
    
    /**
     * Get the current odds for a specific horse
     * 
     * @param horse The horse to get odds for
     * @return The odds, or 0 if the horse is not in the current race
     */
    public double getOddsForHorse(Horse horse) {
        return currentOdds.getOrDefault(horse, 0.0);
    }
    
    /**
     * Calculate the potential payout for a bet
     * 
     * @param horse The horse to bet on
     * @param amount The bet amount
     * @return The potential payout
     */
    public double calculatePotentialPayout(Horse horse, double amount) {
        double odds = getOddsForHorse(horse);
        return amount * odds;
    }
    
    /**
     * Place a bet on a horse
     * 
     * @param horse The horse to bet on
     * @param amount The bet amount
     * @return The placed bet
     * @throws VirtualWallet.InsufficientFundsException if there are not enough funds
     * @throws IllegalStateException if a race is in progress
     * @throws IllegalArgumentException if the bet amount is invalid
     */
    public Bet placeBet(Horse horse, double amount) 
            throws VirtualWallet.InsufficientFundsException, IllegalStateException, IllegalArgumentException {
        // Add this at the beginning of your BettingService.placeBet method
        // Check if race is in proper state for betting
        if (raceInProgress) {
            throw new IllegalStateException("Cannot place bets while a race is in progress");
        }

        // With this:
        RaceManager raceManager = RaceManagerSingleton.getInstance();
        if (raceManager == null) {
            System.err.println("WARNING: RaceManager is null in BettingService, creating default instance");
            // Create and set a new instance if null
            raceManager = new RaceManager();
            RaceManagerSingleton.setInstance(raceManager);
        }

        // Now check the race status
        if (raceManager.getRaceStatus() != RaceManager.RaceStatus.PENDING) {
            throw new IllegalStateException("Cannot place bets while a race is in progress or completed");
        }
        
        // Check if the horse is in the current race
        if (!currentOdds.containsKey(horse)) {
            throw new IllegalArgumentException("Horse is not in the current race");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Bet amount must be positive");
        }
        
        // Check if there are sufficient funds and withdraw the amount
        wallet.withdraw(amount);
        
        // Rest of the method remains the same...
        // Create the bet with current odds
        double odds = currentOdds.get(horse);
        Bet bet = new Bet(horse, amount, odds);
        
        // Add to betting history
        bettingHistory.addBet(bet);
        
        // Recalculate odds after the bet
        recalculateOdds();
        
        // Fire bet placed event
        fireBetPlacedEvent(bet);
        
        return bet;
    }
    
    /**
     * Settle all outstanding bets after a race
     * 
     * @param winner The winning horse
     * @return Total payout amount
     */
    public double settleRace(Horse winner) {
        if (!raceInProgress) {
            throw new IllegalStateException("No race in progress to settle");
        }
        
        double totalPayout = 0.0;
        
        // Get all unsettled bets
        List<Bet> unsettledBets = new ArrayList<>();
        for (Bet bet : bettingHistory.getAllBets()) {
            if (!bet.isSettled()) {
                unsettledBets.add(bet);
            }
        }
        
        // Settle each bet
        for (Bet bet : unsettledBets) {
            double payout = bet.settle(winner);
            totalPayout += payout;
        }
        
        // Add the payout to the wallet
        if (totalPayout > 0) {
            wallet.addFunds(totalPayout);
        }
        
        // Race is no longer in progress
        raceInProgress = false;
        
        // Fire race settled event
        fireRaceSettledEvent(winner, totalPayout);
        
        return totalPayout;
    }
    
    /**
     * Start a new race and calculate initial odds
     * 
     * @param horses The horses in the race
     * @param track The track for the race
     */
    public void startRace(List<Horse> horses, Track track) {
        if (raceInProgress) {
            throw new IllegalStateException("A race is already in progress");
        }
        
        // Calculate initial odds
        currentOdds = oddsCalculator.calculateOdds(horses, track, bettingHistory);
        
        // Set race in progress
        raceInProgress = true;
        
        // Fire odds changed event
        fireOddsChangedEvent();
    }
    
    /**
     * Recalculate odds during a race (e.g., due to new bets or track condition changes)
     */
    public void recalculateOdds() {
        // Get current horses
        List<Horse> horses = new ArrayList<>(currentOdds.keySet());
        
        // Need to get the track from the race manager
        Track track = RaceManagerSingleton.getInstance().getTrack();
        
        // Calculate new odds
        currentOdds = oddsCalculator.calculateOdds(horses, track, bettingHistory);
        
        // Fire odds changed event
        fireOddsChangedEvent();
    }
    
    /**
     * End the current race without settling bets
     * (used if race is canceled)
     */
    public void endRace() {
        if (!raceInProgress) {
            return;
        }
        
        raceInProgress = false;
        
        // Fire race ended event
        fireRaceEndedEvent();
    }
    
    /**
     * Check if a race is currently in progress
     * 
     * @return true if a race is in progress, false otherwise
     */
    public boolean isRaceInProgress() {
        return raceInProgress;
    }
    
    /**
     * Get the betting history
     * 
     * @return The betting history
     */
    public BettingHistory getBettingHistory() {
        return bettingHistory;
    }
    
    /**
     * Get the virtual wallet
     * 
     * @return The virtual wallet
     */
    public VirtualWallet getWallet() {
        return wallet;
    }
    
    /**
     * Reset the betting system
     * Clears betting history and resets the wallet
     */
    public void reset() {
        // Create a new betting history
        bettingHistory = new BettingHistory();
        
        // Reset the wallet
        wallet.reset();
        
        // Clear current odds
        currentOdds.clear();
        
        // Fire reset event
        fireResetEvent();
    }
    
    // Event handling
    
    /**
     * Add a betting service listener
     * 
     * @param listener The listener to add
     */
    public void addBettingServiceListener(BettingServiceListener listener) {
        listenerList.add(BettingServiceListener.class, listener);
    }
    
    /**
     * Remove a betting service listener
     * 
     * @param listener The listener to remove
     */
    public void removeBettingServiceListener(BettingServiceListener listener) {
        listenerList.remove(BettingServiceListener.class, listener);
    }
    
    /**
     * Fire an odds changed event
     */
    protected void fireOddsChangedEvent() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == BettingServiceListener.class) {
                ((BettingServiceListener) listeners[i+1]).oddsChanged(new BettingServiceEvent(this));
            }
        }
    }
    
    /**
     * Fire a bet placed event
     * 
     * @param bet The bet that was placed
     */
    protected void fireBetPlacedEvent(Bet bet) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == BettingServiceListener.class) {
                ((BettingServiceListener) listeners[i+1]).betPlaced(new BettingServiceEvent(this, bet));
            }
        }
    }
    
    /**
     * Fire a race settled event
     * 
     * @param winner The winning horse
     * @param totalPayout The total payout
     */
    protected void fireRaceSettledEvent(Horse winner, double totalPayout) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == BettingServiceListener.class) {
                ((BettingServiceListener) listeners[i+1]).raceSettled(new BettingServiceEvent(this, winner, totalPayout));
            }
        }
    }
    
    /**
     * Fire a race ended event
     */
    protected void fireRaceEndedEvent() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == BettingServiceListener.class) {
                ((BettingServiceListener) listeners[i+1]).raceEnded(new BettingServiceEvent(this));
            }
        }
    }
    
    /**
     * Fire a reset event
     */
    protected void fireResetEvent() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == BettingServiceListener.class) {
                ((BettingServiceListener) listeners[i+1]).bettingSystemReset(new BettingServiceEvent(this));
            }
        }
    }
    
    /**
     * Event class for betting service events
     */
    public static class BettingServiceEvent {
        private BettingService source;
        private Bet bet;
        private Horse winningHorse;
        private double payout;
        
        public BettingServiceEvent(BettingService source) {
            this.source = source;
        }
        
        public BettingServiceEvent(BettingService source, Bet bet) {
            this.source = source;
            this.bet = bet;
        }
        
        public BettingServiceEvent(BettingService source, Horse winningHorse, double payout) {
            this.source = source;
            this.winningHorse = winningHorse;
            this.payout = payout;
        }
        
        public BettingService getSource() {
            return source;
        }
        
        public Bet getBet() {
            return bet;
        }
        
        public Horse getWinningHorse() {
            return winningHorse;
        }
        
        public double getPayout() {
            return payout;
        }
    }
    
    /**
     * Interface for betting service event listeners
     */
    public interface BettingServiceListener extends EventListener {
        void oddsChanged(BettingServiceEvent event);
        void betPlaced(BettingServiceEvent event);
        void raceSettled(BettingServiceEvent event);
        void raceEnded(BettingServiceEvent event);
        void bettingSystemReset(BettingServiceEvent event);
    }
}