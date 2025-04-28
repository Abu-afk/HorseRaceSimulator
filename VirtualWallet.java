/**
 * Manages virtual currency for betting.
 * Tracks balance, prevents over-betting, and handles transactions.
 */
public class VirtualWallet {
    private double balance;
    private static final double INITIAL_BALANCE = 1000.0; // Starting balance
    
    /**
     * Constructor for a new wallet with default initial balance
     */
    public VirtualWallet() {
        this.balance = INITIAL_BALANCE;
    }
    
    /**
     * Constructor with a custom initial balance
     * 
     * @param initialBalance The initial balance for the wallet
     */
    public VirtualWallet(double initialBalance) {
        this.balance = initialBalance;
    }
    
    /**
     * Get the current balance
     * 
     * @return The current balance
     */
    public double getBalance() {
        return balance;
    }
    
    /**
     * Add funds to the wallet
     * 
     * @param amount The amount to add
     * @return The new balance
     * @throws IllegalArgumentException if amount is negative
     */
    public double addFunds(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative amount");
        }
        
        balance += amount;
        return balance;
    }
    
    /**
     * Withdraw funds from the wallet
     * 
     * @param amount The amount to withdraw
     * @return The new balance
     * @throws IllegalArgumentException if amount is negative
     * @throws InsufficientFundsException if there are not enough funds
     */
    public double withdraw(double amount) throws InsufficientFundsException {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot withdraw negative amount");
        }
        
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        
        balance -= amount;
        return balance;
    }
    
    /**
     * Check if there are sufficient funds for a transaction
     * 
     * @param amount The amount to check
     * @return true if there are sufficient funds, false otherwise
     */
    public boolean hasSufficientFunds(double amount) {
        return balance >= amount;
    }
    
    /**
     * Reset the wallet to the initial balance
     * 
     * @return The new balance
     */
    public double reset() {
        balance = INITIAL_BALANCE;
        return balance;
    }
    
    /**
     * Exception thrown when attempting to withdraw more than the available balance
     */
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}