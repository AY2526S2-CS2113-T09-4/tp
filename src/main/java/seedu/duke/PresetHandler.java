package seedu.duke;

import java.util.ArrayList;
import java.util.List;

public class PresetHandler {

    public static List<Posting> generatePostings(String presetInput) {
        if (presetInput == null || presetInput.trim().isEmpty()) {
            throw new IllegalArgumentException("Preset details cannot be empty.");
        }

        // Split by space. Example: "DAILYEXPENSE 50.00"
        String[] parts = presetInput.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid preset format. Use: TYPE AMOUNT (e.g. DAILYEXPENSE 50.00)");
        }

        String type = parts[0].toUpperCase();
        double amount;
        try {
            amount = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preset amount must be a valid number.");
        }

        List<Posting> postings = new ArrayList<>();

        switch (type) {
        case "DAILYEXPENSE":
            // Generic: Out of Cash into Expenses
            postings.add(new Posting("Expenses", amount));
            postings.add(new Posting("Assets:Cash", -amount));
            break;

        case "INCOME":
            // Generic: From Income into Bank
            postings.add(new Posting("Assets:Bank", amount));
            postings.add(new Posting("Income", -amount));
            break;

        case "BUYINGSTOCKS":
            // Generic: Swap Cash for Investments
            postings.add(new Posting("Assets:Investment", amount));
            postings.add(new Posting("Assets:Cash", -amount));
            break;

        default:
            throw new IllegalArgumentException("Unknown preset type: " + type +
                    ". Available: DAILYEXPENSE, INCOME, BUYINGSTOCKS");
        }

        return postings;
    }
}
