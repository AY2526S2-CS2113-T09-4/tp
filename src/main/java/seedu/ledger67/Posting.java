package seedu.ledger67;

public class Posting {
    private final Account account;
    private final double amount;

    public Posting(String accountName, double amount) {
        this.account = new Account(accountName);

        if (Math.abs(amount) < 0.0000001) {
            throw new IllegalArgumentException("Posting amount cannot be zero.");
        }

        this.amount = amount;
    }

    /**
     * Uses the root account type to determine the internal sign convention.
     */
    public double getInternalAmount() {
        String root = account.getRoot();

        if (root.equals("assets") || root.equals("expenses")) {
            return amount;
        } else {
            return -amount;
        }
    }

    public String getAccountName() {
        return account.getFullName();
    }

    public Account getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }
}
