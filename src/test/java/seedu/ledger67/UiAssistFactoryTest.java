package seedu.ledger67;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

public class UiAssistFactoryTest {

    private Scanner createScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    @Test
    public void getInteractiveArguments_addManual_returnsCorrectFlags() {
        // Sequence: 1 (Manual), 01/01/2024 (Date), SGD (Curr), Dinner (Desc),
        // Assets:Cash -10 (Posting), [Enter] (Finish)
        String input = "1\n01/01/2024\nSGD\nDinner\n2\nAssets:Cash -10\nExpenses:Food 10\n";
        Scanner scanner = createScanner(input);

        String result = UiAssistFactory.getInteractiveArguments("add", scanner);

        assertTrue(result.contains("-date 01/01/2024"));
        assertTrue(result.contains("-c SGD"));
        assertTrue(result.contains("-desc Dinner"));
        assertTrue(result.contains("-p \"Assets:Cash -10\""));
        assertTrue(result.contains("-p \"Expenses:Food 10\""));
    }

    @Test
    public void getInteractiveArguments_addPreset_returnsCorrectFlags() {
        // Sequence: 2 (Preset), 01/01/2024 (Date), USD (Curr), INCOME (Type), 500
        // (Amt), [Enter] (No Desc)
        String input = "2\n01/01/2024\nUSD\nINCOME\n500\n\n";
        Scanner scanner = createScanner(input);

        String result = UiAssistFactory.getInteractiveArguments("add", scanner);

        assertTrue(result.contains("-preset \"INCOME 500\""));
        assertTrue(result.contains("-c USD"));
    }

    @Test
    public void getInteractiveArguments_deleteSingleId_returnsId() {
        // Sequence: 1 (ID), 5 (ID Value)
        String input = "1\n5\n";
        Scanner scanner = createScanner(input);

        String result = UiAssistFactory.getInteractiveArguments("delete", scanner);

        assertEquals("5", result);
    }

    @Test
    public void getInteractiveArguments_rates_returnsRefresh() {
        Scanner scanner = createScanner("");
        String result = UiAssistFactory.getInteractiveArguments("rates", scanner);
        assertEquals("refresh", result);
    }

    @Test
    public void getInteractiveArguments_help_returnsEmpty() {
        Scanner scanner = createScanner("");
        String result = UiAssistFactory.getInteractiveArguments("help", scanner);
        assertEquals("", result);
    }

    @Test
    public void getInteractiveArguments_listWithFilters_returnsFlags() {
        // Sequence: MyBank (Acc), [Enter], [Enter], Lunch (Regex), [Enter]
        String input = "MyBank\n\n\nLunch\n\n";
        Scanner scanner = createScanner(input);

        String result = UiAssistFactory.getInteractiveArguments("list", scanner);

        assertTrue(result.contains("-acc MyBank"));
        assertTrue(result.contains("-match Lunch"));
        assertFalse(result.contains("-begin"));
    }

    @Test
    public void init_factoryCall_doesNotThrowException() {
        // Verifies the factory is accessible and responds to unknown commands
        // gracefully
        Scanner scanner = createScanner("");
        String result = UiAssistFactory.getInteractiveArguments("unknown", scanner);
        assertEquals("", result);
    }

    @Test
    public void promptForEdit_fullUpdate_returnsFormattedString() {
        // Inputs:
        // 1. ID: 10
        // 2. New Date: 02/02/2025
        // 3. New Desc: NewLunch
        // 4. New Curr: EUR
        // 5. Overwrite Postings: y
        // 6. Posting: Food 20.00
        // 7. Stop: [Enter]
        String input = "10\n02/02/2025\nNewLunch\nEUR\ny\n2\nFood 20.00\nAssets:Cash -20.00\n";
        Scanner scanner = createScanner(input);

        String result = UiAssistFactory.getInteractiveArguments("edit", scanner);

        assertTrue(result.startsWith("10"));
        assertTrue(result.contains("-date 02/02/2025"));
        assertTrue(result.contains("-desc NewLunch"));
        assertTrue(result.contains("-c EUR"));
        assertTrue(result.contains("-p \"Food 20.00\""));
        assertTrue(result.contains("-p \"Assets:Cash -20.00\""));
    }

    @Test
    public void promptForEdit_partialUpdateNoPostings_returnsFormattedString() {
        // Inputs:
        // 1. ID: 5
        // 2. Date: [Skip]
        // 3. Desc: OnlyDescUpdate
        // 4. Curr: [Skip]
        // 5. Overwrite Postings: n
        String input = "5\n\nOnlyDescUpdate\n\nn\n";
        Scanner scanner = createScanner(input);

        String result = UiAssistFactory.getInteractiveArguments("edit", scanner);

        assertEquals("5 -desc OnlyDescUpdate", result.trim());
        assertFalse(result.contains("-p"));
    }

}
