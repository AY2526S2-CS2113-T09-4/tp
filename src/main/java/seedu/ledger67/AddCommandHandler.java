package seedu.ledger67;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command handler for the 'add' command.
 * Demonstrates how to use ValidationUtils and Config classes for cleaner code.
 */
public class AddCommandHandler implements CommandHandler {
    
    @Override
    public void execute(String args, 
                       TransactionsList list, 
                       CurrencyConverter converter,
                       ExchangeRateStorage exchangeRateStorage,
                       LiveExchangeRateService liveExchangeRateService) {
        Map<String, List<String>> map = Parser.parseArguments(args);
        String dateStr = Parser.getFirstElementFromMap(map, "-date");
        String desc = Parser.getFirstElementFromMap(map, "-desc");
        String currencyStr = Parser.getFirstElementFromMap(map, "-c");
        
        // Use ValidationUtils for input validation
        LocalDate date = ValidationUtils.parseDate(dateStr, "date");
        String description = ValidationUtils.validateDescription(desc, "description");
        String currency = ValidationUtils.validateCurrency(currencyStr, "currency");
        
        // Check for preset
        String presetData = Parser.getFirstElementFromMap(map, "-preset");
        List<Posting> postings;
        
        if (presetData != null) {
            // Use ValidationUtils for preset validation
            String presetType = presetData.split("\\s+")[0];
            ValidationUtils.validatePresetType(presetType, "preset type");
            
            postings = PresetHandler.generatePostings(presetData);
            // If user didn't provide a description, use the preset name as a default
            if (desc == null) {
                description = presetType;
            }
        } else {
            // Fallback to standard manual posting logic
            List<String> postingStrings = map.get("-p");
            if (postingStrings == null) {
                throw new IllegalArgumentException(ErrorMessages.MISSING_POSTINGS);
            }
            postings = Parser.convertStringList2PostingList(postingStrings);
            
            // Validate each posting account
            for (Posting posting : postings) {
                ValidationUtils.validateAccount(posting.getAccountName(), "account");
            }
        }
        
        Transaction transaction = new Transaction(date.format(Config.DATE_FORMATTER), 
                                                 description, postings, currency);
        list.addTransaction(transaction);
        System.out.println(Config.SUCCESS_TRANSACTION_ADDED + " via " + 
                          (presetData != null ? "preset." : "manual input."));
    }
    
    @Override
    public String getCommandName() {
        return "add";
    }
    
    @Override
    public String getHelpText() {
        return "Add a new transaction\n" +
               "Format (Manual): add -date DATE -desc DESCRIPTION -p POSTING1 -p POSTING2 -c CURRENCY\n" +
               "Format (Preset): add -date DATE -preset TYPE AMOUNT -c CURRENCY\n" +
               "Presets: " + Config.getValidPresetTypesString() + "\n" +
               "Example: add -date 18/03/2026 -desc Office supplies " +
               "-p \"Assets:Cash -45.50\" -p \"Expenses:OfficeSupplies 45.50\" -c SGD";
    }
    
    /**
     * Helper method to parse arguments (extracted from Parser for reuse).
     * This demonstrates how to extract common functionality.
     */
    public static Map<String, List<String>> parseArguments(String args) {
        Map<String, List<String>> map = new HashMap<>();
        String[] tokens = (" " + args).split("(?=\\s-[a-zA-Z])");
        
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }
            
            if (token.startsWith("-")) {
                int spaceIdx = token.indexOf(' ');
                String key;
                String value;
                
                if (spaceIdx != -1) {
                    key = token.substring(0, spaceIdx).trim();
                    value = token.substring(spaceIdx + 1).trim();
                } else {
                    key = token;
                    value = "";
                }
                
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        return map;
    }
}