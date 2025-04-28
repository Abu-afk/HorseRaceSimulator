import java.util.Date;

/**
 * Represents a bet placed on a horse in a race.
 * Stores information about the bet amount, odds, result, and payout.
 */
public class Bet {
    private Horse horse;          // The horse the bet is placed on
    private double amount;        // The amount of the bet
    private double odds;          // The odds at the time of the bet
    private boolean settled;      // Whether the bet has been settled
    private boolean won;          // Whether the bet was won
    private double payout;        // The payout amount if the bet was won
    private Date timestamp;       // When the bet was placed
    
    /**
     * Constructor for a new bet
     * 
     * @param horse The horse to bet on
     * @param amount The amount to bet
     * @param odds The odds at the time of the bet
     */
    public Bet(Horse horse, double amount, double odds) {
        this.horse = horse;
        this.amount = amount;
        this.odds = odds;
        this.settled = false;
        this.won = false;
        this.payout = 0.0;
        this.timestamp = new Date();
    }
    
    /**
     * Get the horse that was bet on
     * 
     * @return The horse
     */
    public Horse getHorse() {
        return horse;
    }
    
    /**
     * Get the amount that was bet
     * 
     * @return The bet amount
     */
    public double getAmount() {
        return amount;
    }
    
    /**
     * Get the odds at the time of the bet
     * 
     * @return The odds
     */
    public double getOdds() {
        return odds;
    }
    
    /**
     * Check if the bet has been settled
     * 
     * @return true if the bet has been settled, false otherwise
     */
    public boolean isSettled() {
        return settled;
    }
    
    /**
     * Check if the bet was won
     * 
     * @return true if the bet was won, false otherwise
     */
    public boolean isWon() {
        return won;
    }
    
    /**
     * Get the payout amount if the bet was won
     * 
     * @return The payout amount
     */
    public double getPayout() {
        return payout;
    }
    
    /**
     * Get the timestamp when the bet was placed
     * 
     * @return The timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Calculate the potential payout based on bet amount and odds
     * 
     * @return The potential payout
     */
    public double getPotentialPayout() {
        return amount * odds;
    }
    
    /**
     * Settle the bet with a winning horse
     * 
     * @param winningHorse The horse that won the race
     * @return The payout amount if this bet won, 0 otherwise
     */
    public double settle(Horse winningHorse) {
        if (settled) {
            return payout; // Already settled
        }
        
        settled = true;
        
        if (horse.equals(winningHorse)) {
            won = true;
            payout = getPotentialPayout();
            return payout;
        } else {
            won = false;
            payout = 0.0;
            return 0.0;
        }
    }
    
    /**
     * Get a string representation of the bet
     */
    @Override
    public String toString() {
        String status = settled ? (won ? "Won" : "Lost") : "Pending";
        return String.format("%s: %.2f on %s at %.1f:1 [%s]", 
                            timestamp, amount, horse.getName(), odds, status);
    }
}