# Project Portfolio: Ledger67

## Overview
Ledger67 is a command-line double-entry accounting system designed to help individuals and small businesses manage their financial transactions efficiently. Built on the principles of double-entry bookkeeping, Ledger67 ensures that every financial transaction is recorded with both debit and credit entries, maintaining the fundamental accounting equation: **Assets = Equity - Liabilities + (Income - Expenses)**.

My role involved architecting the core transaction model, implementing the primary command suite, designing the "UI Assist" interaction flow, and establishing the project's technical documentation and testing infrastructure.

### Summary of Contributions

*   **Code contributed**: [Link to my code on the tP Code Dashboard](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=pangdx&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=)
*   **Enhancements implemented**:
    *   **Atomic Transaction & Posting Model**: Transitioned the application from a simple single-value system to a robust double-entry "Postings" model.
        *   **What it does**: Allows a single transaction to be split across multiple accounts (e.g., spending $50 on "Groceries" from "Bank Account").
        *   **Justification**: This is the backbone of bookkeeping. It ensures that money is not just "spent," but moved between accounts, ensuring the ledger always balances.* 
    *   **Transaction Validation Logic**:
        *   **What it does**: Implemented a validation engine that checks if the sum of all debits and credits in a transaction equals zero. If the transaction is unbalanced, the system rejects the entry and provides an error message.
        *   **Justification**: This acts as a critical integrity guardrail for double-entry bookkeeping, preventing users from accidentally corrupting their financial data and ensuring the accounting equation remains perfectly balanced at all times.
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
    *   **Comprehensive Testing & Quality Assurance**:
        *   **What it does**: Authored exhaustive JUnit test suites for all core components, maintaining a high percentage of code coverage across the transaction and filtering modules.
        *   **Justification**: Utilized Jacoco to monitor coverage and identify edge cases, ensuring that the complex logic behind hierarchical account matching and transaction balancing remains robust and bug-free during refactoring.

*   **Contributions to the User Guide (UG)**:
    *   Documented the transition to the `-flag` syntax (e.g., `-date`, `-desc`, `-p`, `-c`) to ensure consistency across the application.
    *   Added detailed sections for **UI Assist**, explaining the transition between manual and guided input.
    *   Wrote the "Understanding Double-Entry Accounting" section to educate users on hierarchical accounts (e.g., `Assets:Cash`).
    *   Created the comprehensive **Command Summary** table for quick user reference.
    *   Added relevant and detailed usage examples for every command to improve user comprehension and minimize learning curves.
    *   Conducted a comprehensive formatting audit across the entire UG to ensure consistent visual styles and professional presentation.

*   **Contributions to the Developer Guide (DG)**:
    *   **Scaffolding**: Authored the initial comprehensive structure of the DG, establishing the template for the team.
    *   **Design Documentation**: Authored sections on the **Atomic Transaction Model** and the **Filtering Logic Architecture**.
    *   **UML Diagrams**: Created and maintained several PlantUML (PUML) diagrams, including:
        *   **Sequence Diagrams**: To illustrate the execution flow of `add -preset` and the `uiassist` logic.
        *   **Architecture Design**: Authored and maintained the high-level **Architecture Diagram**, illustrating the interactions between the UI, Parser, Logic, Model, and Storage components.
        *   **Visual Logic Flow**: Created detailed **Sequence Diagrams** for all primary features, including `add`, `edit`, and `uiassist`, to map out object-oriented execution paths.
    * **Design Rationale**: Authored "Design Considerations" for every major feature, documenting the trade-offs and alternative implementations evaluated by the team during the development cycle.

*   **Contributions to team-based tasks**:
    *   **Refactoring for Modularity**: Lead the effort to decouple the `Parser` logic from the `Command` execution logic, making the codebase more testable and organized.
    *   **Testing Infrastructure**: Configured **Jacoco** for code coverage and maintained the `runtest.sh` scripts for local data cleanup.
    *   **Build Management**: Updated `build.gradle` to include system-wide **Assertions** to catch logical errors in development.
    *   **Project Management**: Set up the GitHub Issues and Milestone labels to track feature progress.
    *   **Testing Management**: Directed the end-to-end testing efforts and performed manual regression testing to identify edge cases in the transaction filtering logic.
    *   **Issue Tracking**: Managed the GitHub issue tracker, ensuring tasks were clearly defined, prioritized, and assigned to prevent developmental bottlenecks.
    *   **Contribution Monitoring**: Performed periodic reviews of the project dashboard to ensure all team members received appropriate credit for their contributions.

---

## Contributions to the User Guide (Extracts)

## Features

### 1. UI Assist: `uiassist`
Toggles whether the user is using UI Assist in which the program helps the user guide through the input or the user is using the standard tagging system

**Format:** `uiassist -on/-off`
`uiassist -on` toggles the UI Assistance on. The user is now using prompted inputs to fill in the details
`uiassist -off` toggles the UI Assistance off. The user is now using the standard tagging system

### 2. Adding a Transaction: `add`
Adds a new financial transaction to your ledger.

**Format (Manual)**: `add -date DATE -desc DESCRIPTION -p POSTING1 -p POSTING2 -c CURRENCY`

**Parameters**
- `-date`: The date of the transaction (e.g., `DD/MM/YYYY`).
- `-desc`: A brief description of the transaction.
- `-p`: A posting containing an account name and an amount (enclosed in quotes). You must have at least two postings.
- `-c`: The currency code (e.g., SGD, USD, EUR).

**Example**
```
add -date 18/03/2026 -desc "Office supplies" 
-p "Assets:Cash -45.50"
-p "Expenses:OfficeSupplies 45.50" 
-c SGD
```

**Format (Presets)**: `add -date DATE -preset TYPE AMOUNT -c CURRENCY`


**Parameters**
- `-date`: The date of the transaction (e.g., `DD/MM/YYYY`).
- `-desc`: A brief description of the transaction.
- `-presets`: Presets: DAILYEXPENSE, INCOME, BUYINGSTOCKS
- `-c`: The currency code (e.g., SGD, USD, EUR).

**Notes**
- Account roots are not case-sensitive
- Sub-account names are case-sensitive, this is to preserve acronyms and other valid mixed-cased names.
- Amounts are stored using standard currency precision of **2 decimal places**.
- If an amount is entered with more than 2 decimal places, Ledger67 will round it to 2 decimal places before storing it.

**Example**
```
add -date 18/03/2026 -preset DAILYEXPENSE 50.00 -c SGD
```

**Example Output**
```
Enter Command: add -date 18/03/2026 -preset DAILYEXPENSE 50.00 -c SGD
Transaction added successfully via preset.
======================================================================
Enter Command: list
ID: 4 | Date: 18/03/2026 | Desc: DAILYEXPENSE | [SGD]
    Expenses                       :      50.00
    Assets:Cash                    :     -50.00
======================================================================
```

### 3. Listing and Filtering Transactions: `list`
Displays recorded transactions. You can view all transactions or use filters to narrow down the results by date, account, keyword, or currency.

**Format**: `list [-acc ACCOUNT] [-begin DATE] [-end DATE] [-match REGEX] [-to CURRENCY]`

**Parameters**:
- `-acc ACCOUNT`: Only show transactions containing this account (and only show that specific account's line).
- `-begin DATE`: Show transactions on or after this date (DD/MM/YYYY).
- `-end DATE`: Show transactions on or before this date (DD/MM/YYYY).
- `-match REGEX`: Show transactions where the description matches the given keyword or regular expression.
- `-to CURRENCY`: Display all values converted to a specific currency for viewing purposes.

> **Note on Currency**: When using `-to`, the displayed values are **view-only**. To permanently save these converted values to your ledger, follow up with the `confirm` command.

**Examples**:
*   **View everything**: `list`
*   **View January food expenses**: `list -acc Expenses:Food -begin 01/01/2026 -end 31/01/2026`
*   **Search for any "Lunch" or "Dinner"**: `list -match "(Lunch|Dinner)"`
*   **View all expenses in USD**: `list -acc Expenses -to USD`

#### Example Output
```

ID: 2 | Date: 2026-03-19 | Desc: Salary | [USD]
Assets:Bank:DBS              :    3000.00

ID: 3 | Date: 2026-03-20 | Desc: Transfer | [SGD]
Assets:Cash                  :    -500.00

```

---

#### Combining with Currency View

You can combine filtering with currency display:

```

list -acc Assets -to USD

```

This will:
- Filter transactions under `Assets`
- Display converted values in USD

---

#### Notes
- This is a **view feature** — it does not modify stored data.
- Filtering works based on account hierarchy (not simple text matching).

---

#### View Converted Values of All Listed Transactions: `list -to TARGET_CURRENCY`
Views all existing transactions to a different currency.

**Format**: `list -to TARGET_CURRENCY`

**Parameters**:
- `-to TARGET_CURRENCY`: Target currency code - `SGD`, `USD`, or `EUR`

**Example**:
```
list -to SGD
```
Output:
```
ID: 1 | Date: 18/03/2026 | Desc: Office supplies | [SGD -> USD]
    Assets:Cash                    :     -45.50 | Display: -33.97 USD
    Expenses:OfficeSupplies        :      45.50 | Display: 33.97 USD
```
**Note**
- This is a view-mode feature only. It does NOT overwrite the stored transaction currency or amount.
- To store conversions:
    - Use `confirm all` to store all transactions
    - Use `confirm ID` to store a specific transaction

---

### 4. Editing a Transaction: `edit`
Modifies an existing transaction.

**Format**: `edit ID [-date DATE] [-desc DESC] [-p POSTING] [-c CURRENCY]`

**Parameters**:
- `ID`: The transaction ID to edit (shown in `list` command)
- Field updates (any combination):
  - `-date NEW_DATE`: Update transaction date
  - `-desc NEW_DESCRIPTION`: Update description
  - `-p POSTING`: New `Postings` that replaces old ones. At least 2. Transaction balance is checked.
  - `-c NEW_CURRENCY`: Update currency

**Examples**:
```
edit 1 -desc "Office stationery purchase" 
edit 2 -c EUR
```

**Example Output**
```
Enter Command: list
ID: 4 | Date: 18/03/2026 | Desc: DAILYEXPENSE | [SGD]
    Expenses                       :      50.00
    Assets:Cash                    :     -50.00
======================================================================
Enter Command: edit 4 -c EUR
Transaction edited successfully.
======================================================================
Enter Command: list
ID: 4 | Date: 18/03/2026 | Desc: DAILYEXPENSE | [EUR]
    Expenses                       :      50.00
    Assets:Cash                    :     -50.00
======================================================================
```


**Note**
- Edited posting amounts are also stored to **2 decimal places**.
- If more than 2 decimal places are entered, Ledger67 will round them before saving.

### 5. Deleting Transactions: `delete`
Removes transactions from the ledger. You can delete a single transaction by its ID or remove multiple transactions at once using filters.

#### Single Deletion
**Format**: `delete ID`
*   **Example**: `delete 3`

#### Bulk Deletion
**Format**: `delete [-begin DATE] [-end DATE] [-match REGEX] [-acc ACCOUNT]`

**Parameters**:
- `-begin DATE`: Delete transactions starting from this date (DD/MM/YYYY).
- `-end DATE`: Delete transactions up to this date (DD/MM/YYYY).
- `-match REGEX`: Delete transactions matching a specific keyword/description.
- `-acc ACCOUNT`: Delete transactions that involve a specific account.

**Examples**:
*   **Delete by Keyword**: `delete -match Steak` (Removes all transactions with "Steak" in the description).
*   **Delete by Date Range**: `delete -begin 01/01/2026 -end 15/01/2026` (Removes everything in the first half of January).
*   **Layered Deletion**: `delete -acc Expenses:Entertainment -match Netflix` (Removes Netflix transactions specifically from that account).


**Example Output**
```
Enter Command: list
ID: 1 | Date: 18/03/2026 | Desc: Office supplies | [SGD]
    Assets:Cash                    :     -45.50
    Expenses:OfficeSupplies        :      45.50
ID: 2 | Date: 18/03/2026 | Desc: Office supplies | [SGD]
    Assets:Cash                    :     -45.50
    Expenses:OfficeSupplies        :      45.50
ID: 3 | Date: 18/03/2026 | Desc: Office supplies | [SGD]
    Assets:Cash                    :     -45.50
    Expenses:OfficeSupplies        :      45.50
======================================================================
Enter Command: delete -match Office
Successfully deleted 3 transaction(s) matching criteria.
======================================================================
Enter Command: list
No matching transactions found.
```

---

## Contributions to the Developer Guide (Extracts)

### 2.1 Architecture Overview

Ledger67 is built on a layered architecture that emphasizes data integrity through double-entry bookkeeping principles. It follows a **Model-View-Controller (MVC)** inspired pattern, adapted for a stateful Command Line Interface (CLI).

![Architecure Diagram](./diagrams/architecture.png)

1.  **Main (Ledger67)**: The entry point. It initializes the infrastructure (Storage, Currency Services), loads existing data, and bootstraps the `Parser` to start the execution loop.
2.  **Controller (Parser & UiAssistFactory)**: 
    *   **Parser**: The central engine that interprets CLI inputs. It features a **Stateful Workflow** for multi-step commands (e.g., `convert` → `confirm`).
    *   **UiAssistFactory**: An interactive layer that intercepts commands when "UI Assist" mode is active, prompting users for required fields to build valid CLI arguments.
3.  **Logic Layer (TransactionsList & PresetHandler)**:
    *   **TransactionsList**: The domain manager. It handles filtering (Regex, Date, Account), CRUD operations, and invokes the `BalanceSheet` engine.
    *   **PresetHandler**: A factory that transforms shorthand keywords (e.g., `DAILYEXPENSE`) into full double-entry posting sets.
4.  **Model Layer (Transaction, Posting, Account)**: Encapsulates the financial logic, ensuring that every entry adheres to the fundamental accounting equation.
5.  **Persistence Layer (Storage & ExchangeRateStorage)**: Handles flat-file persistence (TSV for transactions, JSON for exchange rates).
6.  **Currency Engine**: A specialized subsystem consisting of a `LiveExchangeRateService` (API client), `CurrencyConverter`, and `CurrencyValidator`.

---

### 2.2 Design Considerations

**1. Double-Entry Integrity**  
Unlike simple expense trackers, Ledger67 enforces a "Balanced" rule for every transaction. A transaction is only committed if the sum of its internal amounts equals zero, following the equation:  
Assets = Liabilities + Equity

Where Equity is affected by Income and Expenses:
Equity = Initial Equity + (Income - Expenses)

**2. Hierarchical Account System**  
Accounts are not flat strings; they are hierarchical objects (`Account`). This allows users to categorize finances using colons (e.g., `Assets:Bank:DBS`) and enables the system to perform "roll-up" reporting where a filter for `Assets` includes all sub-accounts.

**3. Hybrid Input Modes**  
The system bridges the gap between power users and beginners. It supports a **Flag-based CLI** (fast, regex-compatible) and an **Interactive UI Assist** mode (guided, prompt-based) that generates the underlying CLI commands for the user.

**4. Stateful Confirmation Workflow**  
To prevent accidental data modification during currency conversion or bulk listing, the `Parser` implements a "Pending state." This allows users to preview converted values in "View-Only" mode before explicitly committing them via a `confirm` command.

**5.  Monetary Precision**
Ledger67 stores monetary values using **2 decimal places**, which is standard for currency amounts.

If users enter values with more than 2 decimal places, Ledger67 will round them before storing them.

Examples:
- `100.999999` is stored as `101.00`
- `50.1` is stored as `50.10`
- `75.555` is stored as `75.56`

**5.  Sign Convention**
- Ledger67 uses a **signed amount model** instead of debit/credit.
- Ledger67 only checks that transactions are **balanced**, and does not enforce sign conventions.
- Users are encouraged to follow the recommended usage for consistency.

---

### 3.1 Transaction Flow
**Implementer: Pran, JJ**

The transaction flow manages the lifecycle of financial records, ensuring that every entry satisfies the fundamental accounting equation before being persisted.

#### 3.1.1 Create (Add) Flow
The addition process supports two paths: **Manual Entry** (providing specific postings) or **Preset Entry** (using templates).
*   **Input**: If `isUiAssistOn` is true, the `UiAssistFactory` interactively prompts the user for fields. Otherwise, the `Parser` extracts flags directly.
*   **Processing**:
    *   If a `-preset` is used, the `PresetHandler` generates a `List<Posting>` (e.g., swapping Assets for Expenses).
    *   The `Parser` validates the currency and date format.
    *   The `Transaction` object is instantiated and must pass an `isBalanced()` check (Sum of postings ≈ 0).
*   **Finalization**: `TransactionsList` appends the transaction, triggers `Storage` to save, and refreshes the background `balance-sheet.csv`.

![Add Flow Diagram](./diagrams/addtransactionflow.png)

#### 3.1.2 Read (List/Filter) Flow
Listing is non-destructive and supports layered filtering.
*   **Filtering**: `TransactionsList` applies filters in sequence: Date Range → Regex Match → Account Hierarchy (e.g., filtering for `Assets` includes `Assets:Cash`).
*   **View-Only Conversion**: If the `-to` flag is present, the `Parser` sets a **Pending State**. The `TransactionsList` renders the converted values for preview but does not modify the underlying data unless a `confirm` command follows.

![List Flow Diagram](./diagrams/listtransactionflow.png)

#### 3.1.3 Update (Edit) Flow
Updates allow modifying any part of an existing transaction while enforcing re-validation.
*   **Lookup**: `TransactionsList` retrieves the transaction by ID.
*   **Modification**: The `Transaction.update()` method selectively replaces fields (Date, Desc, Currency, or Postings).
*   **Validation**: If postings are updated, the `isBalanced()` check is re-run. If the new set is unbalanced, the update is aborted.

![Update Flow Diagram](./diagrams/updatetransactionflow.png)

#### 3.1.4 Delete Flow (Single & Bulk)
Deletion supports targeted removal or mass-clearing based on filters.
*   **Single**: Deletes a specific transaction by its unique ID.
*   **Bulk**: Uses the same filtering engine as the `list` command to identify a sub-set of transactions for removal.
*   **Safety**: Bulk deletion requires at least one filter flag to prevent accidental total wipes (users must use `clear` for that).

![Delete Flow Diagram](./diagrams/deletetransactionflow.png)

---

### 3.2 Updated Transaction Data Model
The `Transaction` model is designed for **Double-Entry Bookkeeping**:

*   **Identity & Metadata**: Contains a unique `int id`, `LocalDate date`, `String description`, and `String currency`.
*   **Postings**: Instead of a single amount, it holds a `List<Posting>`. Each `Posting` links a specific `Account` to a `double` amount.
*   **Account Logic**: The `Account` class validates that the root is one of the five standard types: *Assets, Liabilities, Equity, Income, Expenses*. It handles colon-delimited hierarchy (e.g., `Expenses:Food`).
*   **Balance Integrity**: 
    *   `Posting.getInternalAmount()`: Translates external user numbers into internal accounting signs based on the account type (e.g., a positive number in `Expenses` is a debit, while a positive number in `Income` is a credit).
    *   `Transaction.isBalanced()`: Sums all `internalAmounts`. The transaction is valid only if the total is zero (within an epsilon tolerance of 0.0001 tolerance for floating-point precision).
*   **Persistence**: Serialized as a Tab-Separated Value (TSV) line where postings are encoded as `Account1=Amount;Account2=Amount`.

---

#### 3.2.1 Design Considerations
*   **Validation Timing (Balanced vs. Unbalanced):** 
    *   The system checks if the sum of all postings in a transaction equals zero. 
    *   **Decision:** We allow the creation of an "unbalanced" transaction object in memory during the parsing phase to provide specific feedback (e.g., "Your transaction is off by 5.00"), but we prevent it from being committed to the `TransactionsList` or `Storage`.
*   **Atomicity:** 
    *   By grouping all postings into a single `Transaction` object, we ensure that an "Office Supply" purchase either fails entirely or succeeds entirely. It is impossible to have an expense recorded without a corresponding deduction from assets.

#### 3.2.2 Alternatives Considered
*   **Alternative 1: Flat String Postings:** Storing postings as a simple list of strings within the Transaction class.
    *   **Pros:** Easier to parse and store.
    *   **Cons:** Extremely difficult to perform mathematical validations or hierarchical filtering.
    *   **Decision:** We implemented a dedicated `Posting` class with numeric `amount` and structured `Account` objects to enable rigorous accounting checks.
*   **Alternative 2: Single-entry Legacy System:** Storing one account and one amount per line.
    *   **Pros:** Simpler CLI syntax (`add -a 50 -acc Food`).
    *   **Cons:** Violates fundamental accounting principles and makes it impossible to track where the money came from (e.g., Cash vs. Credit Card).
    *   **Decision:** Shifted to the multi-posting `-p` flag system to support true double-entry bookkeeping.

### Preset Factory Implementation
To simplify the double-entry process, the `PresetFactory` class interprets the `-preset` flag. It maps a single amount to a set of predefined `Posting` objects. For example, a `DAILYEXPENSE` preset automatically generates a negative posting to `Assets:Cash` and a positive posting to `Expenses:General`.

### Filtering Stack Architecture
The filtering logic uses a "Chain of Filters" approach. When a `list` command is executed, the `TransactionList` is passed through a series of predicate filters (Account, Date, and Regex). Only transactions satisfying all active predicates are returned to the UI.

#### Design Consideration: Hierarchical Account Matching
*   **Problem**: Filtering for `Assets` should also return `Assets:Cash` and `Assets:Bank`.
*   **Solution**: Implemented a prefix-matching logic that treats the `:` as a delimiter, ensuring that sub-accounts are correctly identified as part of the parent hierarchy during filtering.
