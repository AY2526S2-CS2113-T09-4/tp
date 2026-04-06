# Project Portfolio: Ledger67

## Overview
Ledger67 is a command-line double-entry accounting system designed to help individuals and small businesses manage their financial transactions efficiently. Built on the principles of double-entry bookkeeping, Ledger67 ensures that every financial transaction is recorded with both debit and credit entries, maintaining the fundamental accounting equation: **Assets = Equity - Liabilities + (Income - Expenses)**.

My role involved architecting the core transaction model, implementing the primary command suite, designing the "UI Assist" interaction flow, and establishing the project's technical documentation and testing infrastructure.

### Summary of Contributions

*   **Code contributed**: [Link to my code on the tP Code Dashboard](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=pangdx&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=)
*   **Enhancements implemented**:
    *   **Atomic Transaction & Posting Model**: Transitioned the application from a simple single-value system to a robust double-entry "Postings" model.
        *   **What it does**: Allows a single transaction to be split across multiple accounts (e.g., spending $50 on "Groceries" from "Bank Account").
        *   **Justification**: This is the backbone of bookkeeping. It ensures that money is not just "spent," but moved between accounts, ensuring the ledger always balances.
    *   **Layered Filtering Stack (Date, Regex, and Account)**:
        *   **What it does**: Implemented a sophisticated filtering system for the `list` and `delete` commands. Users can stack filters like `-acc` (hierarchical account), `-begin`/`-end` (date range), and `-match` (regex description).
        *   **Highlights**: Built the logic to allow these filters to work cumulatively, enabling queries like "View all Food expenses in January containing the word 'Steak'."
    *   **UI Assistance Feature (`uiassist`)**:
        *   **What it does**: Toggles a guided input mode. Instead of typing long command strings, the system prompts the user step-by-step for the date, description, and postings.
        *   **Justification**: Lowers the barrier to entry for users who find complex CLI flags intimidating.
    *   **Preset Transaction Factories**:
        *   **What it does**: Added presets for common transactions via the `-preset` flag (e.g., `DAILYEXPENSE`, `INCOME`).
        *   **Justification**: Simplifies the double-entry process for common tasks by automatically generating the necessary balanced postings for the user.
    *   **Core CRUD Logic**: Refactored the `edit` and `delete` logic to handle multi-posting transactions while maintaining data integrity and logging all activities via the Java Logging API.

*   **Contributions to the User Guide (UG)**:
    *   Documented the transition to the `-flag` syntax (e.g., `-date`, `-desc`, `-p`, `-c`) to ensure consistency across the application.
    *   Added detailed sections for **UI Assist**, explaining the transition between manual and guided input.
    *   Wrote the "Understanding Double-Entry Accounting" section to educate users on hierarchical accounts (e.g., `Assets:Cash`).
    *   Created the comprehensive **Command Summary** table for quick user reference.

*   **Contributions to the Developer Guide (DG)**:
    *   **Scaffolding**: Authored the initial comprehensive structure of the DG, establishing the template for the team.
    *   **Design Documentation**: Authored sections on the **Atomic Transaction Model** and the **Filtering Logic Architecture**.
    *   **UML Diagrams**: Created and maintained several PlantUML (PUML) diagrams, including:
        *   **Sequence Diagrams**: To illustrate the execution flow of `add -preset` and the `uiassist` logic.
        *   **Class Diagrams**: Modeling the relationship between `Transaction`, `Posting`, and the `Account` hierarchy.

*   **Contributions to team-based tasks**:
    *   **Refactoring for Modularity**: Lead the effort to decouple the `Parser` logic from the `Command` execution logic, making the codebase more testable and organized.
    *   **Testing Infrastructure**: Configured **Jacoco** for code coverage and maintained the `runtest.sh` scripts for local data cleanup.
    *   **Build Management**: Updated `build.gradle` to include system-wide **Assertions** to catch logical errors in development.
    *   **Project Management**: Set up the GitHub Issues and Milestone labels to track feature progress.

---

## Contributions to the User Guide (Extracts)

### Adding a Transaction: `add`
Adds a new financial transaction. Every transaction must include at least two postings, and the total debits must equal total credits.

**Manual Format**: `add -date DATE -desc DESCRIPTION -p POSTING1 -p POSTING2 -c CURRENCY`
*   *Example*: `add -date 18/03/2026 -desc "Office supplies" -p "Assets:Cash -45.50" -p "Expenses:OfficeSupplies 45.50" -c SGD`

**Preset Format**: `add -date DATE -preset TYPE AMOUNT -c CURRENCY`
*   *Example*: `add -date 18/03/2026 -preset DAILYEXPENSE 50.00 -c SGD`

### UI Assist: `uiassist`
Toggles between the standard tagging system and a guided prompted input mode.
*   *Format*: `uiassist -on/-off`

---

## Contributions to the Developer Guide (Extracts)

### Preset Factory Implementation
To simplify the double-entry process, the `PresetFactory` class interprets the `-preset` flag. It maps a single amount to a set of predefined `Posting` objects. For example, a `DAILYEXPENSE` preset automatically generates a negative posting to `Assets:Cash` and a positive posting to `Expenses:General`.

### Filtering Stack Architecture
The filtering logic uses a "Chain of Filters" approach. When a `list` command is executed, the `TransactionList` is passed through a series of predicate filters (Account, Date, and Regex). Only transactions satisfying all active predicates are returned to the UI.

#### Design Consideration: Hierarchical Account Matching
*   **Problem**: Filtering for `Assets` should also return `Assets:Cash` and `Assets:Bank`.
*   **Solution**: Implemented a prefix-matching logic that treats the `:` as a delimiter, ensuring that sub-accounts are correctly identified as part of the parent hierarchy during filtering.
