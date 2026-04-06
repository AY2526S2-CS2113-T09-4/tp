package seedu.ledger67;

/**
 * Interface for command handlers in the Ledger67 application.
 * This pattern allows for better separation of concerns and easier testing.
 */
public interface CommandHandler {
    
    /**
     * Executes the command with the given arguments.
     * 
     * @param args the command arguments
     * @param list the transaction list
     * @param converter the currency converter
     * @param exchangeRateStorage the exchange rate storage
     * @param liveExchangeRateService the live exchange rate service
     * @throws IllegalArgumentException if the command arguments are invalid
     */
    void execute(String args, 
                 TransactionsList list, 
                 CurrencyConverter converter,
                 ExchangeRateStorage exchangeRateStorage,
                 LiveExchangeRateService liveExchangeRateService);
    
    /**
     * Gets the command name that this handler processes.
     * 
     * @return the command name
     */
    String getCommandName();
    
    /**
     * Gets the help text for this command.
     * 
     * @return the help text
     */
    String getHelpText();
}