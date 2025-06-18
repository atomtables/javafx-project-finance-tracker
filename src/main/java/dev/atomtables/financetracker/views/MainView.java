package dev.atomtables.financetracker.views;

import dev.atomtables.financetracker.App;
import dev.atomtables.financetracker.Database;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static dev.atomtables.financetracker.App.primaryStage;
import static dev.atomtables.financetracker.NeueDialog.addNeueFinanceToDatabase;

public class MainView implements Initializable {

    // UI labels for greeting and balance
    @FXML private Label whoareyou;
    @FXML private Label mainmoney;

    // UI labels for monthly financial summary
    @FXML private Label monthCreditIn;
    @FXML private Label monthDebitOut;
    @FXML private Label monthPercentIn;
    @FXML private Label monthPercentOut;

    /**
     * Calculates monthly credit and debit summaries and their percentage changes from last month.
     */
    public void calc() throws IOException {
        Database db = Database.database;
        List<Database.Finance> finances = db.getFinances();

        // Calculate date boundaries
        LocalDate now = LocalDate.now();
        LocalDate startOfThisMonth = now.withDayOfMonth(1);
        LocalDate startOfLastMonth = startOfThisMonth.minusMonths(1);

        Date thisMonthStartDate = Date.from(startOfThisMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date lastMonthStartDate = Date.from(startOfLastMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

        double thisMonthCredits = 0, thisMonthDebits = 0;
        double lastMonthCredits = 0, lastMonthDebits = 0;

        // Categorize each transaction by date and type
        for (var finance : finances) {
            Date date = finance.date;
            boolean inThisMonth = !date.before(thisMonthStartDate);
            boolean inLastMonth = date.before(thisMonthStartDate) && !date.before(lastMonthStartDate);

            if (inThisMonth) {
                if (finance.amount > 0) thisMonthCredits += finance.amount;
                else thisMonthDebits += finance.amount;
            } else if (inLastMonth) {
                if (finance.amount > 0) lastMonthCredits += finance.amount;
                else lastMonthDebits += finance.amount;
            }
        }

        // Update UI with formatted data
        monthCreditIn.setText(NumberFormat.getCurrencyInstance().format(thisMonthCredits));
        monthDebitOut.setText(NumberFormat.getCurrencyInstance().format(thisMonthDebits));
        double creditChange = percentChange(lastMonthCredits, thisMonthCredits);
        double debitChange = percentChange(lastMonthDebits, thisMonthDebits);
        monthPercentIn.setText(new DecimalFormat("#0.00").format(creditChange) + "%");
        monthPercentOut.setText(new DecimalFormat("#0.00").format(debitChange) + "%");
    }

    /**
     * Calculates the percent change from oldVal to newVal.
     */
    private double percentChange(double oldVal, double newVal) {
        if (oldVal == 0) return newVal == 0 ? 0 : 100;
        return ((newVal - oldVal) / oldVal) * 100;
    }

    /**
     * Initializes the view with user info and monthly stats.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Greet the user
            whoareyou.setText("Welcome back, " + Database.database.getUser().firstName + "!");
            // Show current balance
            mainmoney.setText(NumberFormat.getCurrencyInstance(Locale.US).format(Database.database.getUser().balance));
            // Calculate and show monthly statistics
            calc();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens the linked bank sync page in browser.
     */
    @FXML void stage3GetCode() throws IOException {
        Desktop.getDesktop().browse(URI.create("https://bankproject.atomtables.dev/"));
    }

    /**
     * Displays a confirmation-style alert about syncing analytics data.
     */
    @FXML void syncTransData() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Syncing Analysation Data");
        a.setHeaderText("Success");
        a.setContentText("Up to date.");
        a.show();
    }

    /**
     * Displays a confirmation-style alert about syncing transaction data.
     */
    @FXML void upToDate() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Syncing Transaction Data");
        a.setHeaderText("Success");
        a.setContentText("Up to date.");
        a.show();
    }

    /**
     * Deletes the local database file and restarts the app after confirmation.
     */
    @FXML void deleteData() throws IOException {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Updating Card Data");
        a.setHeaderText("Alert");
        a.setContentText("Would you like to delete all data? On close, relaunch app.");
        a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        a.setOnCloseRequest(event -> {
            ButtonType result = a.getResult();
            if (result.equals(ButtonType.YES)) {
                File myObj = new File("data.db");
                myObj.delete();
                System.out.println("Restarting app!");
                primaryStage.close();

                // Relaunch the application
                Platform.runLater(() -> {
                    try {
                        new App().start(new Stage());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                System.exit(0);
            }
        });

        a.show();
    }

    /**
     * Opens the dialog to add a new finance record.
     */
    @FXML void addFinance() {
        addNeueFinanceToDatabase(Database.database);
    }

    /**
     * Shows an informational alert about updating/unlinking a card.
     */
    @FXML void updateCard() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Updating Card Data");
        a.setHeaderText("Alert");
        a.setContentText("Would you like to change card data or remove the card?");
        a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        a.setOnCloseRequest(event -> {
            Alert b = new Alert(Alert.AlertType.INFORMATION);
            b.setTitle("Updating Card Data");
            b.setHeaderText("Alert");
            b.setContentText("Delete all data to change/unlink bank.");
            b.show();
        });

        a.show();
    }

    // Navigation methods
    @FXML void goHome() throws IOException {
        App.addRoot("tabs/MainView");
    }

    @FXML void goLogs() throws IOException {
        App.addRoot("tabs/FinanceLogView");
    }

    @FXML void goTrends() throws IOException {
        App.addRoot("tabs/FinanceTrendsView");
    }
}