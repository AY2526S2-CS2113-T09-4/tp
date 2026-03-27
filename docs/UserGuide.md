# User Guide

## Introduction

**Ledger67** is a command-line double-entry accounting system designed to help individuals and small businesses manage their financial transactions efficiently. Built on the principles of double-entry bookkeeping, Ledger67 ensures that every financial transaction is recorded with both debit and credit entries, maintaining the fundamental accounting equation: **Assets = Liabilities + Equity**.

Whether you're a student learning accounting, a small business owner, or someone who wants to better manage personal finances, Ledger67 provides a simple yet powerful way to track your financial activities with proper accounting rigor.

## Quick Start

### Prerequisites
1. Ensure that you have **Java 17 or above** installed on your computer.
2. Download the latest version of `Ledger67` from the project repository.

### Getting Started
#### Option 1: Using the JAR file (Simplest)
1. **Download the application**: Obtain the `tp.jar` file from the latest release.
2. **Run the application**: Open a terminal and navigate to the directory containing `tp.jar`, then run:
   ```
   java -jar tp.jar
   ```

#### Option 2: Using Gradle (For Developers)
If you have the source code and want to build from source:
1. **Navigate to project directory**:
   ```
   cd /path/to/ledger67
   ```
2. **Build the project using Gradle**:
   ```
   ./gradlew build
   ```
3. **Run the application**:
   ```
   ./gradlew run
   ```
   Or to create a runnable JAR:
   ```
   ./gradlew shadowJar
   java -jar build/libs/tp-all.jar
   ```

3. **Start recording transactions**: Use the commands below to add, view, edit, and manage your financial transactions.

## Understanding Double-Entry Accounting

### What is Double-Entry Bookkeeping?
Double-entry bookkeeping is an accounting method where every financial transaction affects at least two accounts. For every debit entry, there must be a corresponding credit entry of equal value. This system ensures that the accounting equation always remains balanced.

### Key Concepts for Beginners

#### 1. The Accounting Equation
```
Assets = Liabilities + Equity
```
- **Assets**: What you own (cash, inventory, equipment)
- **Liabilities**: What you owe (loans, accounts payable)
- **Equity**: Your ownership interest in the business

#### 2. Debit vs. Credit
- **Debit (Dr)**: An entry on the left side of an account
- **Credit (Cr)**: An entry on the right side of an account

#### 3. How Transactions Work in Ledger67
In Ledger67, each transaction you record represents one side of a double-entry. The system automatically ensures that for every transaction you enter, there's an implied corresponding entry to maintain balance.

### Example: Recording a Sale
When you make a sale for $100 cash:
- **Debit**: Cash account increases by $100 (asset increases)
- **Credit**: Revenue account increases by $100 (equity increases)

In Ledger67, you would record this as a single transaction, and the system understands the double-entry implications.

## Features

### Adding a Transaction: `add`
Adds a new financial transaction to your ledger.

**Format**: `add -d DATE -desc DESCRIPTION -a AMOUNT -t TYPE -c CURRENCY`

**Parameters**:
- `-d DATE`: Transaction date in DD/MM/YYYY format (e.g., 18/03/2026)
- `-desc DESCRIPTION`: Brief description of the transaction
- `-a AMOUNT`: Transaction amount (positive number)
- `-t TYPE`: Transaction type - either `debit` or `credit`
- `-c CURRENCY`: Currency code - `SGD`, `USD`, or `EUR`

**Examples**:
```
add -d 18/03/2026 -desc "Office supplies purchase" -a 45.50 -t debit -c SGD
add -d 18/03/2026 -desc "Consulting fee received" -a 500.00 -t credit -c USD
```

### Listing All Transactions: `list`
Displays all recorded transactions in chronological order.

**Format**: `list`

**Example**:
```
list
```
Output:
```
ID: 1 | Date: 18/03/2026 | Desc: Office supplies purchase | Amount: 45.50 | Type: debit | Currency: SGD
ID: 2 | Date: 18/03/2026 | Desc: Consulting fee received | Amount: 500.00 | Type: credit | Currency: USD
```

### Editing a Transaction: `edit`
Modifies an existing transaction.

**Format**: `edit ID [FIELD_UPDATES]`

**Parameters**:
- `ID`: The transaction ID to edit (shown in `list` command)
- Field updates (any combination):
  - `-d NEW_DATE`: Update transaction date
  - `-desc NEW_DESCRIPTION`: Update description
  - `-a NEW_AMOUNT`: Update amount
  - `-t NEW_TYPE`: Update type (debit/credit)
  - `-c NEW_CURRENCY`: Update currency

**Examples**:
```
edit 1 -desc "Office stationery purchase" -a 47.25
edit 2 -t debit -c EUR
```

### Deleting a Transaction: `delete`
Removes a transaction from the ledger.

**Format**: `delete ID`

**Parameters**:
- `ID`: The transaction ID to delete

**Example**:
```
delete 3
```

### Clearing All Transactions: `clear`
Removes all transactions from the ledger (use with caution!).

**Format**: `clear`

**Example**:
```
clear
```

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

### View Converted Values of All Listed Transactions: `list transaction -to`
Views all existing transactions to a different currency.

**Format**: `list transaction -to TARGET_CURRENCY`

**Parameters**:
- `-to TARGET_CURRENCY`: Target currency code - `SGD`, `USD`, or `EUR`

**Example**:
```
list transaction -to SGD
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

### Getting Help: `help`
Displays available commands and usage instructions.

**Format**: `help`

**Example**:
```
help
```

## FAQ

**Q**: How do I transfer my data to another computer?

**A**: Ledger67 currently stores transactions in memory during your session. For data persistence, you would need to implement file storage or database integration in future versions.

**Q**: What's the difference between debit and credit in simple terms?

**A**: Think of debit as "what comes in" or "expenses/increases in assets," and credit as "what goes out" or "income/increases in liabilities or equity." In Ledger67, you record transactions based on how they affect your accounts.

**Q**: Can I use Ledger67 for personal finance tracking?

**A**: Absolutely! While based on double-entry principles used in business accounting, Ledger67 works perfectly for personal finance. Record expenses as debits and income as credits to maintain a clear picture of your financial health.

**Q**: What currencies does Ledger67 support?

**A**: Currently, Ledger67 supports SGD (Singapore Dollar), USD (US Dollar), and EUR (Euro). More currencies can be added in future updates.

**Q**: How does Ledger67 handle the double-entry aspect if I only enter one side?

**A**: Ledger67 is designed as a simplified double-entry system. When you record a transaction, you're entering one side of the entry (e.g., an expense debit). The system assumes there's a corresponding credit to cash or accounts payable, making it easier for users while maintaining accounting principles.

## Command Summary

| Command                      | Format                                                               | Description                       |
|------------------------------|----------------------------------------------------------------------|-----------------------------------|
| **Add**                      | `add -d DATE -desc DESC -a AMOUNT -t TYPE -c CURRENCY`               | Add a new transaction             |
| **List**                     | `list`                                                               | Display all transactions          |
| **Edit**                     | `edit ID [-d DATE] [-desc DESC] [-a AMOUNT] [-t TYPE] [-c CURRENCY]` | Edit an existing transaction      |
| **Delete**                   | `delete ID`                                                          | Remove a transaction              |
| **Clear**                    | `clear`                                                              | Remove all transactions           |
| **Convert**                  | `convert -a AMOUNT -from SOURCE_CURRENCY -to TARGET_CURRENCY`        | Convert currencies                |
| **Convert Transaction**      | `convert transaction ID -to TARGET_CURRENCY`                         | Convert existing transaction      |
| **Convert All Transactions** | `list transaction -to TARGET_CURRENCY`                               | Convert ALL existing transactions |
| **Rates**                    | `rates refresh`                                                      | Refresh live exchange rates       |
| **Confirm** | `confirm`, `confirm all`, `confirm ID` | Store converted transaction(s) |
| **Help**                     | `help`                                                               | Show available commands           |
| **Exit**                     | `exit`                                                               | Exit the application              |

## Tips for Effective Use

1. **Be Consistent**: Use clear, descriptive transaction descriptions that you'll understand later.
2. **Regular Updates**: Record transactions regularly to maintain accurate financial records.
3. **Review Periodically**: Use the `list` command weekly or monthly to review your financial position.
4. **Backup Important Data**: Since transactions are stored in memory, consider exporting or noting down important transactions.
5. **Learn the Basics**: Understanding basic accounting principles will help you get the most out of Ledger67.
6. **Currency Conversion**: Use the view and confirm feature efficiently to better manage multiple currencies,
---

*Ledger67 - Simplifying double-entry accounting for everyone*