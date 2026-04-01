# Project Portfolio: Ledger67

## Overview
Ledger67 is a command-line double-entry accounting system designed to help individuals and small businesses manage their financial transactions efficiently. Built on the principles of double-entry bookkeeping, Ledger67 ensures that every financial transaction is recorded with both debit and credit entries, maintaining the fundamental accounting equation: Assets = Liabilities + Equity.

My role involved architecting the core transaction model to support double-entry accounting, implementing the primary command suite (CRUD operations), and establishing the project's technical documentation and testing infrastructure.

### Summary of Contributions

*   **Code contributed**: [Link to my code on the tP Code Dashboard](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=pangdx&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=)
*   **Enhancements implemented**:
    *   **Atomic Transaction & Posting Model**: Transitioned the application from a simple single-value amount system to a robust "Postings" model.
        *   **What it does**: Allows a single transaction to be split across multiple accounts (e.g., spending $50 on "Groceries" from "Bank Account").
        *   **Justification**: This is the backbone of double-entry bookkeeping. It ensures that money is not just "spent," but moved from one account to another, ensuring the ledger always balances.
        *   **Highlights**: Required a significant refactor of the `Transaction` class and the creation of a new `Posting` class. I replaced the simple `-a` (amount) flag with a repeatable `-p` (posting) flag to handle complex accounting entries.
    *   **Core Financial Operations (CRUD)**: Implemented the logic for `add`, `list`, `edit`, `delete`, and `clear`.
        *   **Highlights**: Specifically handled the complexity of `edit`, ensuring that modifying a multi-posting transaction correctly updates all internal balances and maintains data integrity.
    *   **Logging Infrastructure**: Integrated Java Logging API with varying levels (INFO, WARNING, etc.) across the application to facilitate easier debugging and auditing.
    *   **Regex and Date Filtering**: Allow the user to list transactions with stacking filters like `-acc` and `-match` for Regex
    *   **Presets**: Allow the user to add transactions transactions faster using presets like `add -date 18/03/2026 -preset DAILYEXPENSE 50.00 -c SGD`. Available presets: DAILYEXPENSE, INCOME, BUYINGSTOCKS

*   **Contributions to the User Guide (UG)**:
    *   Documented the new double-entry syntax, specifically explaining how to use the `-p` flag for balanced postings.
    *   Standardized the command format sections for the core financial commands.

*   **Contributions to the Developer Guide (DG)**:
    *   Authored the initial comprehensive structure of the Developer Guide (added over 900 lines of documentation and design specs).
    *   **UML Diagrams**: Created and maintained several PlantUML (PUML) diagrams, including Sequence Diagrams for transaction execution and Class Diagrams for the `Posting` and `Transaction` relationship.
    *   Documented the implementation details of the Atomic Transaction Model and design considerations regarding data storage.

*   **Contributions to team-based tasks**:
    *   **Testing Infrastructure**: Configured **Jacoco** for code coverage reporting and fixed the `runtest.sh` scripts to handle local data directory cleanup, ensuring consistent test environments for the team.
    *   **Build Management**: Updated `build.gradle` to include system-wide **Assertions**, ensuring that development builds catch logical inconsistencies early.
    *   **Code Quality**: Performed checkstyle fixes and refactored static variable ordering to comply with industry standards.
    *   Set up the Issues and Milestone labels.

*   **Review/mentoring contributions**:
    *   Assisted team members in resolving merge conflicts and gaps in understanding during the transition to the double-entry paradigm

---

## Contributions to the User Guide (Extracts)

### Adding a transaction: `add`
Adds a double-entry transaction to the ledger. Every transaction must have at least two postings that balance out.

Format: `add n/DESCRIPTION d/DATE -p ACCOUNT_1 AMOUNT_1 -p ACCOUNT_2 AMOUNT_2`

Example: 
`add n/Buy Grocery d/2026-03-28 -p Expenses:Groceries 50 -p Assets:Bank -50`

---

## Contributions to the Developer Guide (Extracts)

### Atomic Transaction Model
The transition to a double-entry system necessitated an atomic model where a `Transaction` is no longer a single entry, but a collection of `Posting` objects.

#### Implementation
The `Transaction` class contains an `ArrayList<Posting>`. This allows the system to support "Split Transactions" where one debit can be matched against multiple credits.

**Design Consideration: Validation vs. Flexibility**
*   **Option 1 (Strict):** Validate that the sum of postings is zero immediately upon parsing.
*   **Option 2 (Current):** Allow the creation of the object but prevent saving to the ledger if the transaction is unbalanced. 
*   **Reasoning:** Option 2 allows for more helpful error messages to the user, pinpointing exactly how much the transaction is "out of balance" by.
