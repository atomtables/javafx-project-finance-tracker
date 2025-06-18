package dev.atomtables.financetracker;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class NeueDialog {

    // Displays a dialog to enter a new finance record and returns the result as an Optional
    public static Optional<Database.Finance> showDialog() {
        // Create and configure the dialog window
        Dialog<Database.Finance> dialog = new Dialog<>();
        dialog.setTitle("Add Finance Record");
        dialog.setHeaderText("Enter finance details");

        // Define custom "Add" button and add it to the dialog along with the Cancel button
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Set up the layout using a GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10); // Horizontal spacing between columns
        grid.setVgap(10); // Vertical spacing between rows
        grid.setPadding(new Insets(20, 150, 10, 10)); // Padding around the grid

        // Create input fields for name, amount, and type
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<Database.Finance.Type> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Database.Finance.Type.values()); // Add all enum values
        typeCombo.setPromptText("Type");

        // Add labels and inputs to the grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeCombo, 1, 2);

        // Attach the grid to the dialog content
        dialog.getDialogPane().setContent(grid);

        // Handle result conversion when the "Add" button is pressed
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    // Construct a new Finance object based on user input
                    Database.Finance finance = new Database.Finance();
                    finance.name = nameField.getText();
                    finance.amount = Double.parseDouble(amountField.getText());
                    finance.type = typeCombo.getValue();
                    finance.direction = finance.amount >= 0; // Direction: true if incoming
                    finance.date = new java.util.Date(); // Set current date
                    return finance;
                } catch (Exception e) {
                    // Input was invalid (e.g., amount was not a number)
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and wait for the user to respond
        Optional<Database.Finance> result = dialog.showAndWait();
        return result;
    }

    // Utility method to show dialog and add the finance record to the database if confirmed
    public static void addNeueFinanceToDatabase(Database db) {
        Optional<Database.Finance> result = showDialog();
        result.ifPresent(finance -> {
            try {
                db.addFinance(finance); // Add the finance record to the database
            } catch (Exception e) {
                System.out.println("Error adding finance: " + e.getMessage());
            }
        });
    }
}