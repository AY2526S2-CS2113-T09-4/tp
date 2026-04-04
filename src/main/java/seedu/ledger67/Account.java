package seedu.ledger67;

import java.util.Arrays;
import java.util.List;

public class Account {
    private static final List<String> VALID_ROOTS =
            Arrays.asList("assets", "liabilities", "equity", "income", "expenses");

    private final String fullName;
    private final List<String> hierarchy;

    public Account(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be empty.");
        }

        this.fullName = fullName.trim();
        this.hierarchy = Arrays.asList(this.fullName.split(":"));

        // force validation early
        getRoot();
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getHierarchy() {
        return hierarchy;
    }

    public String getRoot() {
        String root = hierarchy.get(0).trim().toLowerCase();
        if (!VALID_ROOTS.contains(root)) {
            throw new IllegalArgumentException(
                    "Invalid account root: " + hierarchy.get(0)
                            + ". Root must be one of Assets, Liabilities, Equity, Income, Expenses.");
        }
        return root;
    }

    public boolean isUnder(String parentAccount) {
        String normalizedParent = parentAccount.trim();
        return fullName.equalsIgnoreCase(normalizedParent)
                || fullName.toLowerCase().startsWith(normalizedParent.toLowerCase() + ":");
    }

    @Override
    public String toString() {
        return fullName;
    }
}
