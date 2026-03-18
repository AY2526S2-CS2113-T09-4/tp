package seedu.duke;

public class Duke {
    /**
     * Main entry-point for the Duke application.
     */
    public static void main(String[] args) {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";

        System.out.println("Hello from\n" + logo);

        Storage storage = new Storage("data/ledger.txt");
        TransactionsList transactionList = new TransactionsList(storage);
        Parser parser = new Parser(transactionList);

        parser.start();

        System.out.println("--- Transaction Manager Exited ---");
    }
}