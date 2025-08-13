package dev.atomtables.financetracker;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Database class handles all database operations for the finance tracker application
 */
public class Database {
    // Singleton instance of the database
    public static final Database database = new Database();

    // JDBC connection string for SQLite database
    public static final String DB = "jdbc:sqlite:data.db";

    /**
     * Constructor initializes the database tables if they do not exist
     */
    private Database() {
        // SQL for user table
        String userTable = """
                CREATE TABLE IF NOT EXISTS user (
                    firstName TEXT NOT NULL,
                    lastName TEXT NOT NULL,
                    email TEXT NOT NULL,
                    dateOfBirth TEXT,
                    occupation TEXT,
                    balance FLOAT DEFAULT 0.0
                );""";
        // SQL for finances table
        String financialRecordTable = """
                CREATE TABLE IF NOT EXISTS finances (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    direction BOOLEAN NOT NULL,
                    amount FLOAT NOT NULL,
                    type TEXT NOT NULL,
                    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        // Create tables if they do not exist
        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.createStatement()) {
            stmt.execute(userTable);
            stmt.execute(financialRecordTable);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * User class represents a user profile in the database
     */
    public static class User {
        public String firstName;
        public String lastName;
        public String email;
        public String dateOfBirth;
        public String occupation;
        public double balance;

        /**
         * Constructor for User
         */
        public User(String firstName, String lastName, String email, String dateOfBirth, String occupation, double balance) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.dateOfBirth = dateOfBirth;
            this.occupation = occupation;
            this.balance = balance;
        }
    }

    /**
     * Finance class represents a financial record
     */
    public static class Finance {
        // Enum for finance types
        public enum Type {
            TRAVEL("TRAVEL"),
            TRANSPORTATION("TRANSPORTATION"),
            FOODDRINK("FOODDRINK"),
            ENTERTAINMENT("ENTERTAINMENT"),
            SERVICES("SERVICES"),
            SHOPPING("SHOPPING"),
            HEALTH("HEALTH");

            public final String value;
            Type(String x) {
                value = x;
            };
        }

        public Integer id; // Unique ID
        public String name; // Name/description
        public Boolean direction; // true = income, false = expense
        public double amount; // Amount of money
        public Type type; // Type/category
        public Date date; // Date of transaction

        // Getters for TableView and other uses
        public double getAmount() { return amount; }
        public String getName() { return name; }
        public Boolean getDirection() { return direction; }
        public Integer getId() { return id; }
        public Type getType() { return type; }
        public Date getDate() { return date; }
    }

    /**
     * Adds a user to the database
     */
    public void setUser(User user) throws IOException {
        String createUserSQL = """
                INSERT INTO user (firstName, lastName, email, dateOfBirth, occupation, balance) VALUES (?,?,?,?,?,?);""";
        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.prepareStatement(createUserSQL)) {
            stmt.setString(1, user.firstName);
            stmt.setString(2, user.lastName);
            stmt.setString(3, user.email);
            stmt.setString(4, user.dateOfBirth);
            stmt.setString(5, user.occupation);
            stmt.setFloat(6, (float) user.balance);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Retrieves the first user from the database
     */
    public User getUser() throws IOException {
        String getUserSQL = "SELECT * FROM user LIMIT 1;";
        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(getUserSQL)) {
            if (!rs.next()) return null;
            return new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getFloat(6));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all finance records from the database
     */
    public ArrayList<Finance> getFinances() throws IOException {
        String getFinancesSQL = "SELECT * FROM finances;";
        ArrayList<Finance> finances = new ArrayList<>();
        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(getFinancesSQL)) {
            while (rs.next()) {
                Finance f = new Finance();
                f.id = rs.getInt(1);
                f.name = rs.getString(2);
                f.direction = rs.getBoolean(3);
                f.amount = rs.getDouble(4);
                f.type = Finance.Type.valueOf(rs.getString(5).toUpperCase());
                f.date = new Date(rs.getTimestamp(6).getTime());
                finances.add(f);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return finances;
    }

    /**
     * Adds a finance record to the database
     */
    public void addFinance(Finance f) throws IOException {
        String addFinanceSQL = """
                INSERT INTO finances (name, direction, amount, type, date) VALUES (?,?,?,?,?);""";
        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.prepareStatement(addFinanceSQL)) {
            stmt.setString(1, f.name);
            stmt.setBoolean(2, f.direction);
            stmt.setDouble(3, f.amount);
            stmt.setString(4, f.type.value);
            stmt.setTimestamp(5, new java.sql.Timestamp(f.date.getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes a finance record by ID
     */
    public void deleteFinance(int id) throws IOException {
        String deleteFinanceSQL = "DELETE FROM finances WHERE id = ?;";
        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.prepareStatement(deleteFinanceSQL)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Seeds the database with example finance records and returns the total value
     */
    public double seedFinancesFromTransactionHistory() throws IOException {
        ArrayList<Finance> financesToAdd = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Finance f1 = new Finance();
            f1.name = "Bella Italia";
            f1.direction = false;
            f1.amount = -3015.0;
            f1.type = Finance.Type.FOODDRINK;
            f1.date = sdf.parse("2025-05-01");
            financesToAdd.add(f1);

            Finance f2 = new Finance();
            f2.name = "Cafe Luxe";
            f2.direction = false;
            f2.amount = -4880.62;
            f2.type = Finance.Type.FOODDRINK;
            f2.date = sdf.parse("2025-05-16");
            financesToAdd.add(f2);

            Finance f3 = new Finance();
            f3.name = "Molecular Munchies";
            f3.direction = false;
            f3.amount = -1666.43;
            f3.type = Finance.Type.FOODDRINK;
            f3.date = sdf.parse("2025-05-06");
            financesToAdd.add(f3);

            Finance f4 = new Finance();
            f4.name = "Dragon Garden";
            f4.direction = false;
            f4.amount = -3705.09;
            f4.type = Finance.Type.FOODDRINK;
            f4.date = sdf.parse("2025-05-05");
            financesToAdd.add(f4);

            Finance f5 = new Finance();
            f5.name = "Atomic Eats";
            f5.direction = false;
            f5.amount = -4092.59;
            f5.type = Finance.Type.FOODDRINK;
            f5.date = sdf.parse("2025-05-21");
            financesToAdd.add(f5);

            Finance f6 = new Finance();
            f6.name = "Ocean's Catch";
            f6.direction = false;
            f6.amount = -2340.26;
            f6.type = Finance.Type.FOODDRINK;
            f6.date = sdf.parse("2025-05-23");
            financesToAdd.add(f6);

            Finance f7 = new Finance();
            f7.name = "Atomic Eats";
            f7.direction = false;
            f7.amount = -2306.63;
            f7.type = Finance.Type.FOODDRINK;
            f7.date = sdf.parse("2025-05-04");
            financesToAdd.add(f7);

            Finance f8 = new Finance();
            f8.name = "Bella Italia";
            f8.direction = false;
            f8.amount = -7361.83;
            f8.type = Finance.Type.FOODDRINK;
            f8.date = sdf.parse("2025-05-01");
            financesToAdd.add(f8);

            Finance f9 = new Finance();
            f9.name = "Atomic Eats";
            f9.direction = false;
            f9.amount = -4413.75;
            f9.type = Finance.Type.FOODDRINK;
            f9.date = sdf.parse("2025-05-13");
            financesToAdd.add(f9);

            Finance f10 = new Finance();
            f10.name = "Atomic Eats";
            f10.direction = false;
            f10.amount = -205.78;
            f10.type = Finance.Type.FOODDRINK;
            f10.date = sdf.parse("2025-05-15");
            financesToAdd.add(f10);

            Finance f11 = new Finance();
            f11.name = "Fusion Bistro";
            f11.direction = false;
            f11.amount = -5601.43;
            f11.type = Finance.Type.FOODDRINK;
            f11.date = sdf.parse("2025-05-17");
            financesToAdd.add(f11);

            Finance f12 = new Finance();
            f12.name = "Salary Deposit - Atomic Corp";
            f12.direction = true;
            f12.amount = 6700.00;
            f12.type = Finance.Type.SERVICES;
            f12.date = sdf.parse("2025-05-02");
            financesToAdd.add(f12);

            Finance f13 = new Finance();
            f13.name = "Salary Deposit - Atomic Corp";
            f13.direction = true;
            f13.amount = 7550.00;
            f13.type = Finance.Type.SERVICES;
            f13.date = sdf.parse("2025-05-16");
            financesToAdd.add(f13);

            Finance f14 = new Finance();
            f14.name = "Gas Station - Molecular Fuel Co";
            f14.direction = false;
            f14.amount = -50.67;
            f14.type = Finance.Type.TRANSPORTATION;
            f14.date = sdf.parse("2025-05-03");
            financesToAdd.add(f14);

            Finance f15 = new Finance();
            f15.name = "Dinner at Skyline";
            f15.direction = false;
            f15.amount = -4150.12;
            f15.type = Finance.Type.FOODDRINK;
            f15.date = sdf.parse("2025-05-14");
            financesToAdd.add(f15);

            Finance f16 = new Finance();
            f16.name = "Elite Eats";
            f16.direction = false;
            f16.amount = -3013.78;
            f16.type = Finance.Type.FOODDRINK;
            f16.date = sdf.parse("2025-05-07");
            financesToAdd.add(f16);

            Finance f17 = new Finance();
            f17.name = "Transfer to Savings";
            f17.direction = false;
            f17.amount = -1000.00;
            f17.type = Finance.Type.SERVICES;
            f17.date = sdf.parse("2025-05-19");
            financesToAdd.add(f17);

            Finance f18 = new Finance();
            f18.name = "Sushi Supreme";
            f18.direction = false;
            f18.amount = -2702.65;
            f18.type = Finance.Type.FOODDRINK;
            f18.date = sdf.parse("2025-05-18");
            financesToAdd.add(f18);

            Finance f19 = new Finance();
            f19.name = "The Golden Spoon";
            f19.direction = false;
            f19.amount = -3920.43;
            f19.type = Finance.Type.FOODDRINK;
            f19.date = sdf.parse("2025-05-20");
            financesToAdd.add(f19);

            Finance f20 = new Finance();
            f20.name = "Salary Deposit - Atomic Corp";
            f20.direction = true;
            f20.amount = 7850.00;
            f20.type = Finance.Type.SERVICES;
            f20.date = sdf.parse("2025-05-30");
            financesToAdd.add(f20);

            Finance f21 = new Finance();
            f21.name = "Skyline Grill";
            f21.direction = false;
            f21.amount = -4250.88;
            f21.type = Finance.Type.FOODDRINK;
            f21.date = sdf.parse("2025-06-01");
            financesToAdd.add(f21);

            Finance f22 = new Finance();
            f22.name = "Atomic Buffet";
            f22.direction = false;
            f22.amount = -1899.99;
            f22.type = Finance.Type.FOODDRINK;
            f22.date = sdf.parse("2025-06-01");
            financesToAdd.add(f22);

            Finance f23 = new Finance();
            f23.name = "Salary Deposit - Atomic Corp";
            f23.direction = true;
            f23.amount = 6950.00;
            f23.type = Finance.Type.SERVICES;
            f23.date = sdf.parse("2025-06-02");
            financesToAdd.add(f23);

            Finance f24 = new Finance();
            f24.name = "Molecular Munchies";
            f24.direction = false;
            f24.amount = -2050.75;
            f24.type = Finance.Type.FOODDRINK;
            f24.date = sdf.parse("2025-06-02");
            financesToAdd.add(f24);

            Finance f25 = new Finance();
            f25.name = "Fusion Feast";
            f25.direction = false;
            f25.amount = -3720.50;
            f25.type = Finance.Type.FOODDRINK;
            f25.date = sdf.parse("2025-06-03");
            financesToAdd.add(f25);

            Finance f26 = new Finance();
            f26.name = "Gourmet Vault";
            f26.direction = false;
            f26.amount = -4899.00;
            f26.type = Finance.Type.FOODDRINK;
            f26.date = sdf.parse("2025-06-03");
            financesToAdd.add(f26);

            Finance f27 = new Finance();
            f27.name = "Sky Sushi";
            f27.direction = false;
            f27.amount = -3550.00;
            f27.type = Finance.Type.FOODDRINK;
            f27.date = sdf.parse("2025-06-04");
            financesToAdd.add(f27);

            Finance f28 = new Finance();
            f28.name = "Salary Deposit - Atomic Corp";
            f28.direction = true;
            f28.amount = 8100.00;
            f28.type = Finance.Type.SERVICES;
            f28.date = sdf.parse("2025-06-04");
            financesToAdd.add(f28);

            Finance f29 = new Finance();
            f29.name = "Moonlight Meals";
            f29.direction = false;
            f29.amount = -2850.66;
            f29.type = Finance.Type.FOODDRINK;
            f29.date = sdf.parse("2025-06-05");
            financesToAdd.add(f29);

            Finance f30 = new Finance();
            f30.name = "Elite Sushi";
            f30.direction = false;
            f30.amount = -4632.43;
            f30.type = Finance.Type.FOODDRINK;
            f30.date = sdf.parse("2025-06-05");
            financesToAdd.add(f30);

            Finance f31 = new Finance();
            f31.name = "Skyline Rooftop Dining";
            f31.direction = false;
            f31.amount = -5250.23;
            f31.type = Finance.Type.FOODDRINK;
            f31.date = sdf.parse("2025-06-06");
            financesToAdd.add(f31);

            Finance f32 = new Finance();
            f32.name = "The Truffle Table";
            f32.direction = false;
            f32.amount = -3789.99;
            f32.type = Finance.Type.FOODDRINK;
            f32.date = sdf.parse("2025-06-06");
            financesToAdd.add(f32);

            Finance f33 = new Finance();
            f33.name = "Atomic Cafe";
            f33.direction = false;
            f33.amount = -1675.45;
            f33.type = Finance.Type.FOODDRINK;
            f33.date = sdf.parse("2025-06-06");
            financesToAdd.add(f33);

            Finance f34 = new Finance();
            f34.name = "Cafe Luxe";
            f34.direction = false;
            f34.amount = -3900.75;
            f34.type = Finance.Type.FOODDRINK;
            f34.date = sdf.parse("2025-06-07");
            financesToAdd.add(f34);

            Finance f35 = new Finance();
            f35.name = "Atomic Bistro";
            f35.direction = false;
            f35.amount = -2600.00;
            f35.type = Finance.Type.FOODDRINK;
            f35.date = sdf.parse("2025-06-07");
            financesToAdd.add(f35);

            Finance f36 = new Finance();
            f36.name = "Salary Deposit - Atomic Corp";
            f36.direction = true;
            f36.amount = 7250.00;
            f36.type = Finance.Type.SERVICES;
            f36.date = sdf.parse("2025-06-08");
            financesToAdd.add(f36);

            Finance f37 = new Finance();
            f37.name = "La Fusion";
            f37.direction = false;
            f37.amount = -4325.89;
            f37.type = Finance.Type.FOODDRINK;
            f37.date = sdf.parse("2025-06-08");
            financesToAdd.add(f37);

            Finance f38 = new Finance();
            f38.name = "Sky View Dine";
            f38.direction = false;
            f38.amount = -3922.77;
            f38.type = Finance.Type.FOODDRINK;
            f38.date = sdf.parse("2025-06-09");
            financesToAdd.add(f38);

            Finance f39 = new Finance();
            f39.name = "Gastronome's Table";
            f39.direction = false;
            f39.amount = -4000.00;
            f39.type = Finance.Type.FOODDRINK;
            f39.date = sdf.parse("2025-06-09");
            financesToAdd.add(f39);

            Finance f40 = new Finance();
            f40.name = "Salary Deposit - Atomic Corp";
            f40.direction = true;
            f40.amount = 7850.00;
            f40.type = Finance.Type.SERVICES;
            f40.date = sdf.parse("2025-06-10");
            financesToAdd.add(f40);

            Finance f41 = new Finance();
            f41.name = "The Ember Lounge";
            f41.direction = false;
            f41.amount = -4870.35;
            f41.type = Finance.Type.FOODDRINK;
            f41.date = sdf.parse("2025-06-10");
            financesToAdd.add(f41);

            Finance f42 = new Finance();
            f42.name = "Prestige Plates";
            f42.direction = false;
            f42.amount = -3900.15;
            f42.type = Finance.Type.FOODDRINK;
            f42.date = sdf.parse("2025-06-10");
            financesToAdd.add(f42);

            Finance f43 = new Finance();
            f43.name = "Salary Deposit - Atomic Corp";
            f43.direction = true;
            f43.amount = 8800.00;
            f43.type = Finance.Type.SERVICES;
            f43.date = sdf.parse("2025-06-10");
            financesToAdd.add(f43);

            Finance f44 = new Finance();
            f44.name = "Nuclear Noodles";
            f44.direction = false;
            f44.amount = -2520.80;
            f44.type = Finance.Type.FOODDRINK;
            f44.date = sdf.parse("2025-06-10");
            financesToAdd.add(f44);

            Finance f45 = new Finance();
            f45.name = "Gilded Grains";
            f45.direction = false;
            f45.amount = -4400.00;
            f45.type = Finance.Type.FOODDRINK;
            f45.date = sdf.parse("2025-06-10");
            financesToAdd.add(f45);

            Finance f46 = new Finance();
            f46.name = "Luxe Tapas Lounge";
            f46.direction = false;
            f46.amount = -3725.55;
            f46.type = Finance.Type.FOODDRINK;
            f46.date = sdf.parse("2025-06-10");
            financesToAdd.add(f46);

            Finance f47 = new Finance();
            f47.name = "Elevated Eats";
            f47.direction = false;
            f47.amount = -3880.99;
            f47.type = Finance.Type.FOODDRINK;
            f47.date = sdf.parse("2025-06-10");
            financesToAdd.add(f47);

            Finance f48 = new Finance();
            f48.name = "Crystalline Kitchen";
            f48.direction = false;
            f48.amount = -4099.40;
            f48.type = Finance.Type.FOODDRINK;
            f48.date = sdf.parse("2025-06-10");
            financesToAdd.add(f48);

            Finance f49 = new Finance();
            f49.name = "Opulent Oyster Bar";
            f49.direction = false;
            f49.amount = -2980.60;
            f49.type = Finance.Type.FOODDRINK;
            f49.date = sdf.parse("2025-06-10");
            financesToAdd.add(f49);

            Finance f50 = new Finance();
            f50.name = "Vaulted Vegan";
            f50.direction = false;
            f50.amount = -3250.35;
            f50.type = Finance.Type.FOODDRINK;
            f50.date = sdf.parse("2025-06-10");
            financesToAdd.add(f50);

            Finance f51 = new Finance();
            f51.name = "Quantum Quiche Cafe";
            f51.direction = false;
            f51.amount = -2480.45;
            f51.type = Finance.Type.FOODDRINK;
            f51.date = sdf.parse("2025-06-10");
            financesToAdd.add(f51);

            Finance f52 = new Finance();
            f52.name = "Gastronome Gala";
            f52.direction = false;
            f52.amount = -4600.00;
            f52.type = Finance.Type.FOODDRINK;
            f52.date = sdf.parse("2025-06-10");
            financesToAdd.add(f52);

            Finance f53 = new Finance();
            f53.name = "Aurora Appetites";
            f53.direction = false;
            f53.amount = -3875.67;
            f53.type = Finance.Type.FOODDRINK;
            f53.date = sdf.parse("2025-06-10");
            financesToAdd.add(f53);

            Finance f54 = new Finance();
            f54.name = "Salary Deposit - Atomic Corp";
            f54.direction = true;
            f54.amount = 9400.00;
            f54.type = Finance.Type.SERVICES;
            f54.date = sdf.parse("2025-06-10");
            financesToAdd.add(f54);

            Finance f55 = new Finance();
            f55.name = "Obsidian Eats";
            f55.direction = false;
            f55.amount = -3410.22;
            f55.type = Finance.Type.FOODDRINK;
            f55.date = sdf.parse("2025-06-10");
            financesToAdd.add(f55);

            Finance f56 = new Finance();
            f56.name = "Golden Grain Eatery";
            f56.direction = false;
            f56.amount = -3620.78;
            f56.type = Finance.Type.FOODDRINK;
            f56.date = sdf.parse("2025-06-10");
            financesToAdd.add(f56);

            Finance f57 = new Finance();
            f57.name = "Binary Bistro";
            f57.direction = false;
            f57.amount = -2800.00;
            f57.type = Finance.Type.FOODDRINK;
            f57.date = sdf.parse("2025-06-10");
            financesToAdd.add(f57);

            Finance f58 = new Finance();
            f58.name = "Copper Fork Lounge";
            f58.direction = false;
            f58.amount = -3775.34;
            f58.type = Finance.Type.FOODDRINK;
            f58.date = sdf.parse("2025-06-10");
            financesToAdd.add(f58);

            Finance f59 = new Finance();
            f59.name = "Dark Matter Dining";
            f59.direction = false;
            f59.amount = -3150.12;
            f59.type = Finance.Type.FOODDRINK;
            f59.date = sdf.parse("2025-06-10");
            financesToAdd.add(f59);

            Finance f60 = new Finance();
            f60.name = "Salary Deposit - Atomic Corp";
            f60.direction = true;
            f60.amount = 8720.00;
            f60.type = Finance.Type.SERVICES;
            f60.date = sdf.parse("2025-06-10");
            financesToAdd.add(f60);

            for (Finance f : financesToAdd) {
                addFinance(f);
            }

            // what is the total value?
            return financesToAdd.stream()
                .mapToDouble(finance -> finance.direction ? finance.amount : -finance.amount)
                .sum();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
