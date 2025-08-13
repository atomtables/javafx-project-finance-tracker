// FinancesView.java
package dev.atomtables.financetracker.views;

import dev.atomtables.financetracker.App;
import dev.atomtables.financetracker.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static dev.atomtables.financetracker.NeueDialog.addNeueFinanceToDatabase;

public class FinanceLogView implements Initializable {
    // FXML-injected UI elements
    @FXML private TableView<Database.Finance> financeTable;

    @FXML private TableColumn<Database.Finance, String> nameColumn;
    @FXML private TableColumn<Database.Finance, String> typeColumn;
    @FXML private TableColumn<Database.Finance, Date> dateColumn;
    @FXML private TableColumn<Database.Finance, Double> amountColumn;

    @FXML private Label currently; // Label for showing the current filter/sort

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        try {
            // Configure table column bindings to Finance object properties
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

            // Format the date column using dd/MM/yyyy
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            dateColumn.setCellFactory(column -> new TableCell<Database.Finance, Date>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        this.setText(new SimpleDateFormat("dd/MM/yyyy").format(item));
                    }
                }
            });

            // Format the amount column as currency and color it green (income) or red (spending)
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            amountColumn.setCellFactory(column -> new TableCell<Database.Finance, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        this.setBackground(Background.EMPTY);
                    } else {
                        if (item < 0) {
                            this.setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(0), null)));
                        } else {
                            this.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(0), null)));
                        }
                        this.setText(NumberFormat.getCurrencyInstance(Locale.US).format(item));
                    }
                }
            });

            // Load finance records, sort by date, then display in reverse (newest first)
            List<Database.Finance> l = Database.database.getFinances();
            l.sort(new DateSort());
            financeTable.getItems().setAll(l.reversed());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Sorts and displays finance records by ascending date
    @FXML void sortByDateAscending() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        l.sort(new DateSort());
        financeTable.getItems().setAll(l);
        currently.setText("Sorted by date (ascending)");
    }

    // Sorts and displays finance records by descending date
    @FXML void sortByDateDescending() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        l.sort(new DateSort());
        financeTable.getItems().setAll(l.reversed());
        currently.setText("Sorted by date (descending)");
    }

    // Sorts and displays finance records by ascending amount
    @FXML void sortByAmountAscending() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        l.sort(new AmountSort());
        financeTable.getItems().setAll(l);
        currently.setText("Sorted by amount (ascending)");
    }

    // Sorts and displays finance records by descending amount
    @FXML void sortByAmountDescending() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        l.sort(new AmountSort());
        financeTable.getItems().setAll(l.reversed());
        currently.setText("Sorted by amount (descending)");
    }

    // Filters and displays only negative amounts (spending)
    @FXML void filterBySpending() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        l.removeIf(finance -> finance.amount >= 0);
        financeTable.getItems().setAll(l.reversed());
        currently.setText("Filtered by spending");
    }

    // Filters and displays only positive amounts (income)
    @FXML void filterByIncome() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        l.removeIf(finance -> finance.amount <= 0);
        financeTable.getItems().setAll(l.reversed());
        currently.setText("Filtered by income");
    }

    // Filters and displays only finance records within the past month
    @FXML void filterByLastMonth() throws IOException {
        List<Database.Finance> l = Database.database.getFinances();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Get date one month ago
        Date oneMonthAgo = cal.getTime();
        l.removeIf(finance -> finance.date.before(oneMonthAgo));
        financeTable.getItems().setAll(l.reversed());
        currently.setText("Filtered by logs within the last month");
    }

    // Navigation methods to different tabs in the application
    @FXML void goHome() throws IOException {
        App.addRoot("tabs/MainView");
    }

    @FXML void goLogs() throws IOException {
        App.addRoot("tabs/FinanceLogView");
    }

    @FXML void goTrends() throws IOException {
        App.addRoot("tabs/FinanceTrendsView");
    }

    // Opens the dialog to add a new finance record
    @FXML void addFinance() {
        addNeueFinanceToDatabase(Database.database);
    }
}

// Comparator class for sorting by date
class DateSort implements Comparator<Database.Finance> {
    @Override
    public int compare(Database.Finance car1, Database.Finance car2) {
        return car1.date.compareTo(car2.date);
    }
}

// Comparator class for sorting by amount
class AmountSort implements Comparator<Database.Finance> {
    @Override
    public int compare(Database.Finance car1, Database.Finance car2) {
        return Double.compare(car1.amount, car2.amount);
    }
}