# Project Portfolio Page – JJ
---

## Overview

**Ledger67** is a command-line double-entry accounting system designed to help users manage financial transactions with proper accounting principles such as Assets = Liabilities + Equity.

Beyond basic transaction tracking, Ledger67 supports hierarchical accounts, multi-currency conversion, confirmation workflows, and real-time balance sheet generation for financial analysis.

---

## Summary of Contributions

### Code Contributed
[View my code contributions](<https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=yeejj&breakdown=true&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&checkedFileTypes=docs~functional-code~test-code~other&filteredFileName=>)

---

### Enhancements Implemented

I implemented several core features that significantly extend the system’s capabilities:

#### 1. Balance Sheet Generation (Major Feature)
- Implemented full balance sheet computation based on:
    - Assets, Liabilities, Equity
    - Dynamic incorporation of Income and Expenses into Equity
- Added support for:
    - Hierarchical filtering (`balance -acc`)
    - Currency conversion (`balance -to`)
    - Combined filtering and conversion
- Designed and implemented a new `BalanceSheet` class for:
    - Output formatting
    - CSV export (`data/balance-sheet.csv`)
- Integrated feature across `Parser`, `TransactionsList`, and `Account`

**Complexity:**
- Required aggregation across all transactions and postings
- Required correct accounting logic (Income → Equity)
- Required integration with currency conversion system
- Required handling of partial (filtered) views
- Required conversion into .csv file

---

#### 2. Currency Conversion System (Major Feature)
- Implemented full conversion pipeline:
    - `CurrencyConverter`
    - `ExchangeRateData`
    - `ExchangeRateStorage`
    - `LiveExchangeRateService`
- Supported:
    - Direct conversion (`convert`)
    - Transaction-level conversion (`convert transaction`)
    - List view conversion (`list -to`)
- Designed conversion to be **non-destructive by default**
- Designed to also cater to live rates through an API

---

#### 3. Confirm Conversion Workflow (Multi-step Feature)
- Designed and implemented a **stateful Parser workflow**
- Added:
    - `confirm`
    - `confirm all`
    - `confirm ID`
- Implemented temporary state handling:
    - pending transaction ID
    - pending currency
    - list-view mode tracking
- Reused existing `editTransaction()` logic to persist updates

**Complexity:**
- Required multi-step command design
- Required careful state management
- Avoided modifying core data model unnecessarily
- Integrated conversion into storage feature

---

#### 4. Hierarchical Account System (Core Feature)
- Implemented `Account` class:
    - Parsing using `:` (e.g., `Assets:Bank:DBS`)
    - Validation of account roots
- Implemented hierarchical filtering:
    - `list -acc`
    - `balance -acc`
- Developed `isUnder()` method for prefix-based matching

---

#### 5. Storage System (Core Feature)
- Designed and implemented full persistence system:
    - `Storage` class
- Features:
    - Save/load transactions to file
    - Immediate persistence after CRUD operations
    - Robust parsing and error handling
- Implemented encoding/decoding to handle special characters

---

#### 6. Parser Enhancements
- Main dependency for the design of all the features mentioned above
    -   Modified the code here to cater for each of the features 
- Implemented:
    - Help command (`help`)
    - Error fallback handling for invalid commands
- Extended parser to support:
    - flag-based parsing (`-acc`, `-to`)
    - multi-step workflows (confirm feature) 

---

#### 7. Testing
- Created **JUnit tests for all implemented features**
- Updated existing tests based on edits, mainly:
    - `ParserTest`
    - `TransactionsListTest`
- Ensured:
    - Compatibility with new features
    - Full pass of `gradlew clean test` and `check`

---

### Contributions to the User Guide

- Added full documentation for:
    - Balance Sheet feature
    - Hierarchical filtering
    - Currency conversion workflows
- Updated:
    - Command summary table
    - Help-related documentation
- Ensured UG remained consistent with implementation after each update

---

### Contributions to the Developer Guide

- Implemented full sections for:
    - Storage Feature
    - Currency Conversion Feature
    - Confirm Conversion Workflow
    - Balance Sheet Feature
- Added:
    - Architectural explanations
    - Design considerations
    - Limitations and trade-offs
- Created multiple UML diagrams:
    - Sequence diagrams for all implemented features
    - Contributed to Class Diagram (added new classes which I contributed)

---

### Contributions to Team-Based Tasks

- Created and managed:
    - Issues
    - Milestones aligned with project deadlines
- Suggested structured milestone planning based on course timeline
- Collaborated closely with pangdx (e.g., class diagram integration)

---

### Review / Mentoring Contributions

- Reviewed and discussed bugs with teammates
- Assisted in debugging feature integrations
- Ensured consistency between components during development
- Ensure consistency in UG and DG for all pushes.

---

### Contributions Beyond the Project Team

- Ensured:
    - Discussion amongst team members to sta on Task with the project.
    - Code quality through testing
    - Documentation consistency across UG and DG
- Provided internal guidance on:
    - Collaborated with Pran to come up with the product
    - feature design decisions
    - system integration approaches
        - Using Sequence Diagrams and Class Diagrams sketches

---

## Contributions to the Developer Guide (Extracts)
### Class Diagrams

The following class diagram shows the relationships between all components:

![Class Diagram](../diagrams/ClassDiagram.png)

### Storage Feature
Implementer: JJ
The storage feature is responsible for persisting transaction data to local file storage and restoring it upon application startup.
---

#### 1. Transaction Persistence
The `Storage` class manages reading from and writing to a local text file (`ledger.txt`).
* Transactions are saved in a tab-separated format
* Each transaction includes ID, date, description, amount, type, and currency
* Data is written immediately after every modifying operation (add, edit, delete, clear)

This ensures:
* Data durability across application runs
* Minimal risk of data loss
---

#### 2. Loading Transactions
Upon application startup, `Storage` loads all previously saved transactions into memory.
* Reads file line-by-line
* Parses each line into a `Transaction` object
* Reconstructs transaction IDs and updates the auto-increment counter

Invalid or malformed lines are safely ignored to prevent crashes.
---

#### 3. Data Encoding and Decoding
To ensure file integrity, special characters are handled using escaping:
* `\t` for tabs
* `\n` for newlines
* `\\` for backslashes
  This prevents corruption of the file format when storing user input.
---

#### 4. Integration with TransactionsList
The `TransactionsList` component interacts directly with `Storage`:
* Calls `load()` during initialization
* Calls `save()` after every modification

This design ensures:
* Separation of concerns between data management and persistence
* Consistent synchronization between memory and disk
---

#### Design Considerations
* **Immediate Persistence**: Data is saved after every operation to prevent loss
* **Simple File Format**: Tab-delimited text allows easy debugging and readability
* **Robustness**: Invalid data is safely handled without crashing the application
* **Encapsulation**: Storage logic is isolated from business logic in `TransactionsList`

#### Sequence Diagram
![Storage Sequence Diagram](../diagrams/StorageSequence.png)

The diagram above shows:
* `TransactionsList` triggering save operations
* `Storage` writing transaction data to file
* Data being reloaded when the application starts
---

### Currency Conversion Feature
Implementer: JJ
The currency conversion feature extends the system by introducing dynamic currency handling, persistent exchange rate storage, and real-time rate retrieval.
---

#### Integration with Architecture
This feature introduces and integrates the following components:
* `CurrencyConverter`: Core conversion logic
* `ExchangeRateData`: Data model for exchange rates
* `ExchangeRateStorage`: Handles persistent storage of exchange rates
* `LiveExchangeRateService`: Fetches real-time exchange rates

These components are connected as follows:
* `Duke` initializes the converter and injects it into `Parser` and `TransactionsList`
* `Parser` handles `convert` and `rates` commands
* `TransactionsList` uses the converter for display-level conversions
* `ExchangeRateStorage` ensures exchange rates persist across application runs
---

#### 1. Simple Currency Conversion
The `CurrencyConverter` class provides the core conversion logic via the `convert()` method.
* Validates currencies using `CurrencyValidator`
* Converts via a base currency for consistency
* Handles same-currency conversion as a no-op
---

#### 2. Exchange Rate Storage
The `ExchangeRateStorage` component ensures exchange rate data is persisted locally.
* Loads exchange rates from a JSON file at application startup
* Saves updated rates after fetching from the API
* Ensures data validity before reading or writing

This allows the application to:
* Avoid repeated API calls
* Maintain functionality even without internet access
---

#### 3. Live Exchange Rate Integration
The `LiveExchangeRateService` fetches real-time exchange rates from an external API.
* Sends HTTP requests to retrieve latest rates
* Parses JSON responses into `ExchangeRateData`
* Updates local storage via `ExchangeRateStorage`
* Triggered using the `rates refresh` command

A fallback mechanism in `Duke` ensures the system remains functional if live data retrieval fails.
---

#### 4. Conversion of Stored Transactions
Stored transactions can be converted dynamically without modifying underlying data.
Implemented in:
* `Parser` (`convert transaction` command)
* `TransactionsList` (conversion during display)

Features:
* Converts a transaction by ID
* Uses latest available exchange rates
* Preserves original stored values
---

#### 5. Display Currency Conversion (List View)
Transactions can be displayed in a selected currency using:
```
list -to USD
```

* Uses `setDisplayCurrency()` and `setAutoConvertDisplay()`
* Conversion is applied during output rendering
* Does not overwrite stored transaction data
---

#### Design Considerations
* **Separation of Concerns**: Conversion, storage, and API handling are modularized
* **Persistence**: Exchange rates are stored locally to improve performance and reliability
* **Resilience**: Fallback data ensures continued functionality without API access

#### Sequence Diagram
![Convert Transaction Sequence Diagram](../diagrams/ConvertTransactionSequence.png)

The diagram above illustrates how user input flows through the system:
* `Parser` extracts and validates inputs
* `CurrencyValidator` ensures valid currencies
* `CurrencyConverter` performs the conversion
* Results are displayed without modifying stored data
---


#### 6. Confirm and Store Converted Transactions
This feature extends the currency conversion system by allowing users to **persist converted values into storage**,
instead of keeping them as view-only.
Previously, all conversion operations (`convert`, `convert transaction`, `list transaction -to`) were strictly **non-destructive**.
---

#### Implementation Details
This feature is implemented primarily in the `Parser` component through **stateful command handling**.

New internal state variables in `Parser`:

* `pendingTransactionId`: stores the transaction ID (for single conversion)
* `pendingTargetCurrency`: stores the selected target currency
* `pendingFromListView`: indicates whether the conversion came from list view
---

##### Workflow
##### Case 1: `convert transaction`
1. User runs:
   ```
   convert transaction 3 -to SGD
   ```
2. System:
    * Calculates converted value
    * Displays result
    * Stores pending state (ID + currency)
3. User enters:
   ```
   confirm
   ```
4. System:
    * Calls `TransactionsList.editTransaction(...)`
    * Updates:
        * amount
        * currency
    * Persists changes via `Storage.save()`
---

##### Case 2: `list -to`
1. User runs:
   ```
   list -to SGD
   ```
2. System:
    * Displays converted values (view-only)
    * Stores pending state (currency + list mode)
3. User options:
    * `confirm all` → update all transactions
    * `confirm ID` → update one transaction
4. System:
    * Iterates through transactions (if all)
    * Applies conversion
    * Persists via `Storage`

---

#### Design Considerations
* **Explicit Confirmation Required**
    * Prevents accidental data mutation
    * Maintains safety of original financial records
* **Stateful Parser Design**
    * Enables multi-step commands (view → confirm)
    * Avoids modifying core model classes
* **Reuse of Existing Logic**
    * Uses `editTransaction()` instead of creating new update methods
---

#### Sequence Diagram
![Confirm Transaction Sequence Diagram](../diagrams/ConfirmTransactionSequence.png)

### Hierarchical Account Registry & Filtering Feature
Implementer: JJ

This feature allows users to filter transactions based on hierarchical account structures using the `list -acc` command.

---
#### Motivation

As the number of transactions grows, users need a way to quickly isolate transactions belonging to specific financial categories such as Assets, Expenses, or Income.
Hierarchical account structures (e.g., `Assets:Bank:DBS`) make simple string matching insufficient. Hence, a structured filtering mechanism was implemented.
---
#### Implementation
This feature is implemented across three main components:

1. **Parser**
    - Detects the `-acc` flag
    - Extracts the account filter string
    - Passes it to `TransactionsList`

2. **Account**
    - Parses hierarchical account names using `:`
    - Provides `isUnder(String parentAccount)` method
    - Supports prefix-based hierarchical matching

3. **TransactionsList**
    - Iterates through all transactions
    - Filters postings based on account hierarchy
    - Displays only matching transactions

---

#### Core Logic
The key method used is:

public boolean isUnder(String parentAccount)

#### Sequence Diagram
![Hierarchical Account Registry & Filtering Feature Diagram](../diagrams/HierachalAccRegFiltering.png)

### Balance Sheet Feature
Implementer: JJ

The Balance Sheet feature allows users to generate a real-time summary of their financial position based on the accounting equation:

```
Assets = Liabilities + Equity

```

It aggregates all transactions and computes totals across hierarchical account categories.
---

#### Integration with Architecture

This feature primarily extends:

- `Parser` → parses `balance` commands
- `TransactionsList` → computes aggregated values
- `Account` → supports hierarchical filtering
- `CurrencyConverter` → enables optional currency conversion

New component:
- `BalanceSheet` → handles formatting and export logic
---

#### 1. Command Handling

The `Parser` handles the following command formats:
```

balance
balance -acc ACCOUNT
balance -to TARGET_CURRENCY
balance -acc ACCOUNT -to TARGET_CURRENCY

````

The parser:
- extracts optional flags (`-acc`, `-to`)
- passes parameters to `TransactionsList.printBalanceSheet(...)`
---

#### 2. Balance Aggregation Logic
Implemented in `TransactionsList`:
```
private Map<String, Double> buildBalanceSheetTotals(...)
````

##### Workflow:

1. Iterate through all transactions
2. Iterate through all postings
3. Apply account filtering:

    * Uses `Account.isUnder(accountPrefix)`
4. Apply currency conversion (if enabled)
5. Aggregate totals into a `Map<String, Double>`

---
#### 3. Hierarchical Account Support

The feature leverages:
```
account.isUnder("Assets:Bank")
```

This enables:

* parent-level aggregation (`Assets`)
* sub-account filtering (`Assets:Bank:DBS`)

---

#### 4. Income and Expense Handling

Income and Expenses are incorporated into Equity:

```
Net Income = Income - Expenses
```
Implementation:

* Income accounts increase equity
* Expense accounts reduce equity
* Computed dynamically during balance generation

This ensures:

```
Assets = Liabilities + Equity
```

---

#### 5. Currency Conversion

If `-to` is specified:

* Each posting is converted using `CurrencyConverter`
* Conversion is applied **during aggregation only**
* Stored data remains unchanged

---

#### 6. Output Formatting

Handled by the `BalanceSheet` class:

* Groups accounts into:

    * Assets
    * Liabilities
    * Equity
* Displays:

    * sub-accounts
    * totals
    * accounting check

---

#### 7. CSV Export

Each execution of `balance` triggers:
```
balanceSheet.exportToCsv("data/balance-sheet.csv");
```

##### File Characteristics:

* Overwrites existing file
* Contains:

    * account names
    * balances
    * report currency

---

#### Design Considerations
**1. Separation of Concerns**
* Aggregation → `TransactionsList`
* Presentation → `BalanceSheet`
  **2. Non-Destructive Operations**

* Conversion is view-only
* No mutation of stored transactions

**3. Reuse of Existing Structures**

* Uses `Posting`, `Account`, `CurrencyConverter`
* Avoids duplicating logic

**4. Hierarchical Scalability**

* Supports deep account nesting without redesign

---

#### Limitations

* Filtered views (`-acc`) may not balance fully
* Mixed currencies require `-to` for meaningful totals
* CSV export is overwrite-only (no versioning)

---

#### Sequence Diagram
![Balance Sheet Sequence Diagram](../diagrams/BalanceSheetSequence.png)

## Contributions to the User Guide (Extracts)
### Currency Conversion: `convert`
Converts amounts between different currencies using current exchange rates.

**Format**: `convert -a AMOUNT -from SOURCE_CURRENCY -to TARGET_CURRENCY`

**Parameters**:
- `-a AMOUNT`: Amount to convert (positive number)
- `-from SOURCE_CURRENCY`: Source currency code - `SGD`, `USD`, or `EUR`
- `-to TARGET_CURRENCY`: Target currency code - `SGD`, `USD`, or `EUR`

**Example**:
```
convert -a 100 -from USD -to SGD
```
Output:
```
100.00 USD = 135.50 SGD
```
**Note**
- This is a view-mode feature only. It does NOT overwrite the stored transaction currency or amount.

### Converting Existing Transactions: `convert transaction`
Converts an existing transaction to a different currency.

**Format**: `convert transaction ID -to TARGET_CURRENCY`

**Parameters**:
- `ID`: The transaction ID to convert (shown in `list` command)
- `-to TARGET_CURRENCY`: Target currency code - `SGD`, `USD`, or `EUR`

**Example**:
```
convert transaction 3 -to SGD
```
Output:
```
Transaction 3: 50.00 USD = 67.75 SGD
```
**Note**
- This is a view-mode feature only. It does NOT overwrite the stored transaction currency or amount.
- To store the converted value, use the `confirm` command.

### View Converted Values of All Listed Transactions: `list -to TARGET_CURRENCY`
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
ID: 1 | Date: 18/03/2026 | Desc: "Office supplies purchase" | Amount: 45.50 | Type: debit | Currency: SGD
ID: 2 | Date: 18/03/2026 | Desc: "Consulting fee received" | Amount: 500.00 | Type: credit | Currency: USD | Display: 669.72 SGD
```
**Note**
- This is a view-mode feature only. It does NOT overwrite the stored transaction currency or amount.
- To store conversions:
    - Use `confirm all` to store all transactions
    - Use `confirm ID` to store a specific transaction

### Refreshing Exchange Rates: `rates`
Refreshes live exchange rates from external sources.

**Format**: `rates refresh`

**Example**:
```
rates refresh
```
Output:
```
Exchange rates refreshed successfully for 2026-03-19.
```

### Confirming Converted Transactions: `confirm`

After using conversion commands, Ledger67 allows you to **store the converted values permanently**.

By default, all conversions are **view-only**. You must explicitly confirm to update the stored transaction(s).

---

#### 1. Confirming a Single Converted Transaction

After using:

```
convert transaction ID -to TARGET_CURRENCY
```

You can store the converted result using:

```
confirm
```

**Example**:

```
convert transaction 3 -to SGD
confirm
```

**Result**:

* The transaction’s **amount** is updated to the converted value
* The transaction’s **currency** is updated to the target currency
* Changes are saved permanently

---

#### 2. Confirming Transactions from List View

After using:

```
list transaction -to TARGET_CURRENCY
```

You can choose to store:

##### a) All transactions

```
confirm all
```

##### b) A specific transaction

```
confirm ID
```

**Example**:

```
list transaction -to SGD
confirm 2
```

---

#### Important Notes

* If you enter any command **other than `confirm`**, the conversion will be **ignored**
* Confirmation updates:

    * transaction **amount**
    * transaction **currency**
* Changes are **saved to storage immediately**

---

### Filtering Transactions by Account: `list -acc`
Allows you to filter transactions based on hierarchical account categories.

Ledger67 supports **hierarchical account names** using `:`. This means accounts can be structured into categories and subcategories such as:

- `Assets:Cash`
- `Assets:Bank:DBS`
- `Expenses:Food`
- `Income:Salary`

#### Format
```

list -acc ACCOUNT

```

#### Examples
```

list -acc Assets
list -acc Assets:Bank
list -acc Expenses

```

#### Behaviour
- Displays only transactions that contain postings under the specified account.
- Supports hierarchical filtering:
    - `Assets` → shows ALL asset-related postings (Cash, Bank, etc.)
    - `Assets:Bank` → shows only bank-related postings
- Filtering is **case-insensitive**.

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

### Viewing the Balance Sheet: `balance`

Displays a summary of your financial position based on the accounting equation:
```
Assets = Liabilities + Equity

```
This command aggregates all transactions and shows totals by account category.
---

#### Basic Format
```
balance
```
#### Example Output
```
===== BALANCE SHEET =====
Scope: ALL ACCOUNTS
Report Currency: ORIGINAL TRANSACTION CURRENCIES

ASSETS
Assets:Cash                          350.00
Assets:Bank:Main                    200.00
Total Assets                        550.00

LIABILITIES
Total Liabilities                     0.00

EQUITY
Equity:Capital                      400.00
Current Period Net Income           150.00
Total Equity                        550.00

CHECK
Total Assets                        550.00
Liabilities + Total Equity          550.00
Equation Status                     PASS
========================================

```

---

### Including Income and Expenses
Ledger67 automatically incorporates:

- **Income** → increases Equity
- **Expenses** → decreases Equity

These are reflected as:
```
Net Income = Income - Expenses
```
This ensures the balance sheet satisfies:
```
Assets = Liabilities + Equity
```

---
### Filtering Balance Sheet by Account: `balance -acc`

Allows you to generate a balance sheet for a specific account hierarchy.

#### Format
```

balance -acc ACCOUNT

```

#### Examples
```

balance -acc Assets
balance -acc Assets:Bank

```

#### Behaviour
- Includes only accounts under the specified hierarchy
- Uses the same hierarchical logic as `list -acc`
- May not balance fully (partial view)

#### Example Output
```

Scope: Assets:Bank
Equation Status: FILTERED VIEW (may not balance)

```

---
### Viewing Balance Sheet in Another Currency: `balance -to`
Displays all values converted into a target currency.

#### Format
```

balance -to TARGET_CURRENCY

```

#### Example
```

balance -to USD

```

#### Behaviour
- Converts all postings into the selected currency
- Uses latest exchange rates
- Does NOT modify stored data

---

### Combining Filters and Conversion

You can combine both options:

```

balance -acc Assets:Bank -to USD

```

---
### Exporting Balance Sheet

Each time the `balance` command is run:

- A CSV file is generated at:
```
data/balance-sheet.csv

```
#### File Contents
The CSV includes:
- Account names
- Aggregated balances
- Report currency

#### Notes
- The file is overwritten each time `balance` is executed
- Useful for Excel analysis or reporting
- Export does NOT affect ledger data

---

### Important Notes

- `balance` is a **read-only feature**
- It does NOT modify any transactions
- Currency conversion is **view-only**
- Filtering may result in non-balanced views
