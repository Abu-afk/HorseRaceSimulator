// BettingHistory.java needs to be updated as the uploaded file appears 
// to be a duplicate of Bet.java. Here's the correct implementation:

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the history of bets for the betting system.
 * Tracks all bets placed, computes statistics, and provides historical analysis.
 */
public class BettingHistory {
    private List<Bet> bets;
    
    /**
     * Constructor for a new betting history
     */
    public BettingHistory() {
        this.bets = new ArrayList<>();
    }
    
    /**
     * Add a bet to the history
     * 
     * @param bet The bet to add
     */
    public void addBet(Bet bet) {
        bets.add(bet);
    }
    
    /**
     * Get all bets in the history
     * 
     * @return List of all bets
     */
    public List<Bet> getAllBets() {
        return new ArrayList<>(bets);
    }
    
    /**
     * Get all settled bets in the history
     * 
     * @return List of settled bets
     */
    public List<Bet> getSettledBets() {
        return bets.stream()
                .filter(Bet::isSettled)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unsettled bets in the history
     * 
     * @return List of unsettled bets
     */
    public List<Bet> getUnsettledBets() {
        return bets.stream()
                .filter(bet -> !bet.isSettled())
                .collect(Collectors.toList());
    }
    
    /**
     * Get all winning bets in the history
     * 
     * @return List of winning bets
     */
    public List<Bet> getWinningBets() {
        return bets.stream()
                .filter(Bet::isWon)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all losing bets in the history
     * 
     * @return List of losing bets
     */
    public List<Bet> getLosingBets() {
        return bets.stream()
                .filter(bet -> bet.isSettled() && !bet.isWon())
                .collect(Collectors.toList());
    }
    
    /**
     * Get the total number of bets placed
     * 
     * @return Total bet count
     */
    public int getTotalBetCount() {
        return bets.size();
    }
    
    /**
     * Get the number of bets placed on each horse
     * 
     * @return Map of horse to bet count
     */
    public Map<Horse, Integer> getBetCountByHorse() {
        Map<Horse, Integer> counts = new HashMap<>();
        
        for (Bet bet : bets) {
            Horse horse = bet.getHorse();
            counts.put(horse, counts.getOrDefault(horse, 0) + 1);
        }
        
        return counts;
    }
    
    /**
     * Get the number of winning bets for each horse
     * 
     * @return Map of horse to win count
     */
    public Map<Horse, Integer> getWinCountByHorse() {
        Map<Horse, Integer> counts = new HashMap<>();
        
        for (Bet bet : getWinningBets()) {
            Horse horse = bet.getHorse();
            counts.put(horse, counts.getOrDefault(horse, 0) + 1);
        }
        
        return counts;
    }
    
    /**
     * Get the number of losing bets for each horse
     * 
     * @return Map of horse to loss count
     */
    public Map<Horse, Integer> getLossCountByHorse() {
        Map<Horse, Integer> counts = new HashMap<>();
        
        for (Bet bet : getLosingBets()) {
            Horse horse = bet.getHorse();
            counts.put(horse, counts.getOrDefault(horse, 0) + 1);
        }
        
        return counts;
    }
    
    /**
     * Get the total amount bet across all bets
     * 
     * @return Total bet amount
     */
    public double getTotalBetAmount() {
        return bets.stream()
                .mapToDouble(Bet::getAmount)
                .sum();
    }
    
    /**
     * Get the total amount bet on a specific horse
     * 
     * @param horse The horse to get bet amount for
     * @return Total bet amount on the horse
     */
    public double getTotalBetOnHorse(Horse horse) {
        return bets.stream()
                .filter(bet -> bet.getHorse().equals(horse))
                .mapToDouble(Bet::getAmount)
                .sum();
    }
    
    /**
     * Get the total winnings from all bets
     * 
     * @return Total winnings
     */
    public double getTotalWinnings() {
        return getWinningBets().stream()
                .mapToDouble(Bet::getPayout)
                .sum();
    }
    
    /**
     * Get the overall win rate (wins / total settled bets)
     * 
     * @return Win rate as a decimal (0.0 to 1.0)
     */
    public double getOverallWinRate() {
        List<Bet> settledBets = getSettledBets();
        if (settledBets.isEmpty()) {
            return 0.0;
        }
        
        return (double) getWinningBets().size() / settledBets.size();
    }
    
    /**
     * Get the win rate for a specific horse
     * 
     * @param horse The horse to get win rate for
     * @return Win rate as a decimal (0.0 to 1.0)
     */
    public double getWinRateForHorse(Horse horse) {
        List<Bet> horseBets = bets.stream()
                .filter(bet -> bet.getHorse().equals(horse) && bet.isSettled())
                .collect(Collectors.toList());
        
        if (horseBets.isEmpty()) {
            return 0.0;
        }
        
        long wins = horseBets.stream()
                .filter(Bet::isWon)
                .count();
        
        return (double) wins / horseBets.size();
    }
    
    /**
     * Clear all bet history
     */
    public void clear() {
        bets.clear();
    }
}