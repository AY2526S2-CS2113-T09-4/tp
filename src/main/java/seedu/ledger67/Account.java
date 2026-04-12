package seedu.ledger67;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Account {
    private static final List<String> VALID_ROOTS =
            Arrays.asList("assets", "liabilities", "equity", "income", "expenses");

    private final String fullName;
    private final List<String> hierarchy;

    public Account(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Account name cannot be empty.");
        }

        List<String> normalizedHierarchy = Arrays.stream(fullName.trim().split(":"))
                .map(String::trim)
                .collect(Collectors.toList());

        if (normalizedHierarchy.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("Account hierarchy cannot contain empty components.");
        }

        this.hierarchy = normalizedHierarchy;
        this.fullName = String.join(":", normalizedHierarchy);

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
        String root = hierarchy.get(0).toLowerCase();
        if (!VALID_ROOTS.contains(root)) {
            throw new IllegalArgumentException(
                    "Invalid account root: " + hierarchy.get(0)
                            + ". Root must be one of Assets, Liabilities, Equity, Income, Expenses.");
        }
        return root;
    }

    public boolean isUnder(String parentAccount) {
        if (parentAccount == null || parentAccount.isBlank()) {
            return false;
        }

        String normalizedParent = normalizeAccountName(parentAccount);
        String lowerFullName = fullName.toLowerCase();
        String lowerParent = normalizedParent.toLowerCase();

        return lowerFullName.equals(lowerParent)
                || lowerFullName.startsWith(lowerParent + ":");
    }

    private static String normalizeAccountName(String accountName) {
        return Arrays.stream(accountName.trim().split(":"))
                .map(String::trim)
                .collect(Collectors.joining(":"));
    }

    @Override
    public String toString() {
        return fullName;
    }
}