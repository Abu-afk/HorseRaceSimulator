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

