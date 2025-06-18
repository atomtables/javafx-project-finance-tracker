package dev.atomtables.financetracker.views;

import dev.atomtables.financetracker.App;
import dev.atomtables.financetracker.Database;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Math.abs;

public class FinanceTrendsView implements Initializable {
    @FXML
    private Label escape;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Database db = Database.database;
        List<Database.Finance> finances = null;
        try {
            finances = db.getFinances();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        double thisMonthCredits = 0, thisMonthDebits = 0;
        double lastMonthCredits = 0, lastMonthDebits = 0;

        for (var finance : finances) {
            if (finance.amount > 0) thisMonthCredits += finance.amount;
            else thisMonthDebits += finance.amount;
        }

        if (abs(thisMonthCredits) > abs(thisMonthDebits)) {
            escape.setText("Good! You are making more than you spend. It is recommended that you take " +
                    "50% of your extra income and place it into investment opportunities, and use the " +
                    "other 50% for a normal Savings account in the need of an emergency.");
        } else {
            escape.setText("Oh no! You are spending more than you make. It is recommended that you cut back " +
                    "on food/drink costs, transportation, or utilities. Try to get a deal from your internet " +
                    "company or avoid the Starbucks latte on the way to work or use a subway train.");
        }
    }

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
