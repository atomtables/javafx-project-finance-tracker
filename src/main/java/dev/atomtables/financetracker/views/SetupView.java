package dev.atomtables.financetracker.views;

import dev.atomtables.financetracker.App;
import dev.atomtables.financetracker.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class SetupView {
    // Stage 1 input fields
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;

    // Stage 2 input fields
    @FXML private Label screen2Label;
    @FXML private DatePicker dateOfBirth;
    @FXML private TextField occupation;

    // Stage 3 UI elements
    @FXML private Button stage3Button;
    @FXML private TextField code;

    // Stored user input (across multiple setup stages)
    private static String SfirstName;
    private static String SlastName;
    private static String Semail;
    private static String SDOB;
    private static String Soccupation;
    private static double sum;

    /**
     * Called when stage 1 is completed.
     * Stores first name, last name, and email, then moves to stage 2.
     */
    @FXML void stage1Complete() throws IOException, InterruptedException {
        SfirstName = firstName.getText();
        SlastName = lastName.getText();
        Semail = email.getText();
        App.addRoot("setup/SetupView2");
    }

    /**
     * Runs when stage 2 is loaded.
     * Updates label to greet the user by name.
     */
    @FXML void stage2Entered() throws IOException {
        screen2Label.setText("Nice to meet you, " + SfirstName + "! Tell us about yourself.");
    }

    /**
     * Called when stage 2 is completed.
     * Stores date of birth and occupation, then moves to stage 3.
     */
    @FXML void stage2Complete() throws IOException {
        SDOB = dateOfBirth.getValue().toString();
        Soccupation = occupation.getText();
        App.addRoot("setup/SetupView3");
    }

    /**
     * Called when user types into the verification code field.
     * Enables the "Next" button.
     */
    @FXML void stage3Modified() throws IOException {
        stage3Button.setDisable(false);
    }

    /**
     * Opens the finance tracking code page in the browser.
     */
    @FXML void stage3GetCode() throws IOException {
        Desktop.getDesktop().browse(URI.create("https://bankproject.atomtables.dev/financetrack"));
    }

    /**
     * Called when stage 3 is completed.
     * Seeds initial finances from transaction history and moves to stage 4.
     */
    @FXML void stage3Complete() throws IOException {
        sum = Database.database.seedFinancesFromTransactionHistory();
        App.addRoot("setup/SetupView4");
    }

    /**
     * Final step: constructs and stores the user object in the database.
     * Then transitions to the main app view.
     */
    @FXML void stage4Complete() throws IOException {
        Database.User user = new Database.User(
                SfirstName,
                SlastName,
                Semail,
                SDOB,
                Soccupation,
                sum
        );
        Database.database.setUser(user);
        App.setRoot("tabs/MainView");
    }

    /**
     * Generic back navigation handler.
     */
    @FXML void back() throws IOException {
        App.goBack();
    }
}