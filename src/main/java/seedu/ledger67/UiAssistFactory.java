package seedu.ledger67;

import java.util.Scanner;

/**
 * Factory to generate command arguments interactively when UI Assist is
 * enabled.
 */
public class UiAssistFactory {

    public static String getInteractiveArguments(String command, Scanner scanner) {
        switch (command.toLowerCase()) {
        case "add":
            return promptForAdd(scanner);
        case "list":
            return promptForList(scanner);
        case "delete":
            return promptForDelete(scanner);
        case "edit":
            return promptForEdit(scanner);
        case "convert":
            return promptForConvert(scanner);
        case "balance":
            return promptForBalance(scanner);
        case "rates":
            return "refresh"; // 'rates' only takes 'refresh'
        case "clear":
        case "help":
            return ""; // These commands don't need arguments
        default:
            return "";
        }
    }

    // ==========================================
    // COMMAND PROMPTERS
    // ==========================================

    private static String promptForAdd(Scanner scanner) {
        StringBuilder args = new StringBuilder();

        System.out.println("How would you like to add the transaction?");
        int choice = askChoice(scanner, "1. Manual Entry", "2. Use Preset (e.g., DAILYEXPENSE, INCOME)");

        args.append(askRequiredFlag(scanner, "Enter date (DD/MM/YYYY)", "-date"));
        args.append(askRequiredFlag(scanner, "Enter currency (SGD, USD, EUR)", "-c"));

        if (choice == 1) {
            // Manual Entry
            args.append(askOptionalFlag(scanner, "Enter description (Press Enter to skip)", "-desc"));

            System.out.println("\n--- Enter Postings (Format: AccountName Amount) ---");
            System.out.println("Example: Assets:Cash -50.00");
            System.out.println("Each transaction must have at least 2 postings.");

            int postingCount;
            while (true) {
                System.out.print("How many postings do you want to enter? ");
                String input = scanner.nextLine().trim();

                try {
                    postingCount = Integer.parseInt(input);
                    if (postingCount >= 2) {
                        break;
                    }
                } catch (NumberFormatException e) {
                    // fall through to error message below
                }

                System.out.println("Please enter a valid integer of at least 2.");
            }

            for (int i = 1; i <= postingCount; i++) {
                String posting = askRequiredValue(scanner, "Enter posting " + i);
                args.append(" -p \"").append(posting).append("\"");
            }
        } else {
            // Preset Entry
            
            System.out.println("\nAvailable Presets: DAILYEXPENSE, INCOME, BUYINGSTOCKS");
            String presetType = askRequiredValue(scanner, "Enter preset type");
            String presetAmount = askRequiredValue(scanner, "Enter preset amount");

            args.append(" -preset \"").append(presetType).append(" ").append(presetAmount).append("\"");
            args.append(askOptionalFlag(scanner, "Enter description (Press Enter to use default)", "-desc"));
        }

        return args.toString().trim();
    }

    private static String promptForList(Scanner scanner) {
        StringBuilder args = new StringBuilder();

        args.append(askOptionalFlag(scanner, "Filter by Account? (Press Enter to skip)", "-acc"));
        args.append(askOptionalFlag(scanner, "Start Date [DD/MM/YYYY]? (Press Enter to skip)", "-begin"));
        args.append(askOptionalFlag(scanner, "End Date [DD/MM/YYYY]? (Press Enter to skip)", "-end"));
        args.append(askOptionalFlag(scanner, "Match description [Regex]? (Press Enter to skip)", "-match"));
        args.append(askOptionalFlag(scanner, "Convert display to Currency? (Press Enter to skip)", "-to"));

        return args.toString().trim();
    }

    private static String promptForDelete(Scanner scanner) {
        StringBuilder args = new StringBuilder();

        System.out.println("How do you want to delete?");
        int choice = askChoice(scanner, "1. Delete by Transaction ID", "2. Bulk delete by filters");

        if (choice == 1) {
            String id = askRequiredValue(scanner, "Enter Transaction ID to delete");
            args.append(id); // Single ID deletion expects just the number
        } else {
            System.out.println("\n--- Enter at least one filter to bulk delete ---");
            args.append(askOptionalFlag(scanner, "Start Date [DD/MM/YYYY]? (Press Enter to skip)", "-begin"));
            args.append(askOptionalFlag(scanner, "End Date [DD/MM/YYYY]? (Press Enter to skip)", "-end"));
            args.append(askOptionalFlag(scanner, "Match description [Regex]? (Press Enter to skip)", "-match"));
            args.append(askOptionalFlag(scanner, "Filter by Account? (Press Enter to skip)", "-acc"));

            if (args.toString().trim().isEmpty()) {
                System.out.println("No filters provided! Deletion aborted.");
                return ""; // Parser will naturally catch the empty args and throw its exception
            }
        }

        return args.toString().trim();
    }

    private static String promptForEdit(Scanner scanner) {
        StringBuilder args = new StringBuilder();

        String id = askRequiredValue(scanner, "Enter Transaction ID to edit");
        args.append(id);

        System.out.println("\n--- Leave fields blank and press Enter to keep existing values ---");
        args.append(askOptionalFlag(scanner, "New Date (DD/MM/YYYY)", "-date"));
        args.append(askOptionalFlag(scanner, "New Description", "-desc"));
        args.append(askOptionalFlag(scanner, "New Currency", "-c"));

        System.out.print("Do you want to overwrite all postings? (y/N): ");
        String overwrite = scanner.nextLine().trim();

        if (overwrite.equalsIgnoreCase("y") || overwrite.equalsIgnoreCase("yes")) {
            System.out.println("\n--- Enter NEW Postings (Format: AccountName Amount) ---");
            System.out.println("Each transaction must have at least 2 postings.");

            int postingCount;
            while (true) {
                System.out.print("How many postings do you want to enter? ");
                String input = scanner.nextLine().trim();

                try {
                    postingCount = Integer.parseInt(input);
                    if (postingCount >= 2) {
                        break;
                    }
                } catch (NumberFormatException e) {
                    // fall through
                }

                System.out.println("Please enter a valid integer of at least 2.");
            }

            for (int i = 1; i <= postingCount; i++) {
                String posting = askRequiredValue(scanner, "Enter posting " + i);
                args.append(" -p \"").append(posting).append("\"");
            }
        }

        return args.toString().trim();
    }

    private static String promptForConvert(Scanner scanner) {
        StringBuilder args = new StringBuilder();

        System.out.println("What do you want to convert?");
        int choice = askChoice(scanner, "1. Convert a simple amount", "2. Convert an existing transaction");

        if (choice == 1) {
            args.append(askRequiredFlag(scanner, "Amount to convert", "-a"));
            args.append(askRequiredFlag(scanner, "From Currency (SGD, USD, EUR)", "-from"));
            args.append(askRequiredFlag(scanner, "To Currency (SGD, USD, EUR)", "-to"));
        } else {
            String id = askRequiredValue(scanner, "Enter Transaction ID to convert");
            String targetCurrency = askRequiredValue(scanner, "Target Currency (SGD, USD, EUR)");
            args.append("transaction ").append(id).append(" -to ").append(targetCurrency);
        }

        return args.toString().trim();
    }

    private static String promptForBalance(Scanner scanner) {
        StringBuilder args = new StringBuilder();

        args.append(askOptionalFlag(scanner, "Filter by Account? (Press Enter for all)", "-acc"));
        args.append(askOptionalFlag(scanner, "Convert display to Currency? (Press Enter to skip)", "-to"));

        return args.toString().trim();
    }

    // ==========================================
    // REUSABLE HELPER METHODS
    // ==========================================

    /**
     * Prompts until the user provides a non-empty string, returns it formatted with
     * the flag.
     */
    private static String askRequiredFlag(Scanner scanner, String prompt, String flag) {
        String input = askRequiredValue(scanner, prompt);
        return " " + flag + " " + input;
    }

    /**
     * Prompts until the user provides a non-empty string, returns just the value.
     */
    private static String askRequiredValue(Scanner scanner, String prompt) {
        String input = "";
        while (input.isEmpty()) {
            System.out.print(prompt + ": ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("This field is required. Please enter a value.");
            }
        }
        return input;
    }

    /**
     * Prompts the user. If they hit Enter without typing, it skips and returns an
     * empty string.
     */
    private static String askOptionalFlag(Scanner scanner, String prompt, String flag) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return "";
        }
        return " " + flag + " " + input;
    }

    /**
     * Prints options and forces the user to pick either 1 or 2.
     */
    private static int askChoice(Scanner scanner, String opt1, String opt2) {
        System.out.println(opt1);
        System.out.println(opt2);

        while (true) {
            System.out.print("Select (1 or 2): ");
            String input = scanner.nextLine().trim();
            if (input.equals("1")){
                return 1;
            }
            if (input.equals("2")){
                return 2;
            }
            System.out.println("Invalid selection. Please enter 1 or 2.");
        }
    }
}
