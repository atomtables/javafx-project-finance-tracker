package dev.atomtables.financetracker;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.WritableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static dev.atomtables.financetracker.Database.DB;
import static java.lang.System.exit;

public class App extends Application {
    public static Scene scene;             // Global reference to the main scene
    public static Stage primaryStage;      // Global reference to the primary stage
    public static StackPane parent;        // The root node of the scene, used for transitions

    private static final ArrayList<String> navStack = new ArrayList<>(); // Navigation history stack

    @Override
    public void start(Stage stage) throws IOException {
        // Load the base layout from App.fxml (likely contains only a StackPane)
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("App.fxml"));
        parent = fxmlLoader.load();
        scene = new Scene(parent);

        // Determine whether a user exists in the database
        if (Database.database.getUser() != null) {
            // If user exists, load the main application view
            Parent r = (new FXMLLoader(App.class.getResource("tabs/MainView.fxml"))).load();
            parent.getChildren().add(r);
            navStack.add("tabs/MainView");
        } else {
            // Otherwise, load the welcome screen
            Parent r = (new FXMLLoader(App.class.getResource("WelcomeView.fxml"))).load();
            parent.getChildren().add(r);
            navStack.add("WelcomeView");
        }

        // Apply application-wide styles
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("design.css")).toExternalForm());

        // Set up and show the main stage
        stage.setTitle("financetrack - Welcome");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(); // Launch JavaFX application
    }

    // Initialize the SQLite database schema
    public static void initSql() {
        String sql1 = "CREATE TABLE IF NOT EXISTS user ("
                + "firstName VARCHAR(255) NOT NULL,"
                + "lastName VARCHAR(255) NOT NULL,"
                + "email VARCHAR(255) NOT NULL"
                + ");";

        try (var conn = DriverManager.getConnection(DB);
             var stmt = conn.createStatement()) {
            stmt.execute(sql1); // Create user table if it doesn't exist
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Loads and transitions to a new root view with a slide-in animation
    static void loadRoot/*with animation*/(String fxml) throws IOException {
        Node old = parent.getChildren().getFirst(); // Get current displayed view
        Parent root = (new FXMLLoader(App.class.getResource(fxml + ".fxml"))).load(); // Load new view

        if (scene != null) {
            // Prepare the new view to slide in from the right
            root.translateXProperty().set(scene.getWidth());
            parent.getChildren().add(root);

            // Animate the new view into place
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(root.translateXProperty(), 0, Interpolator.LINEAR);
            KeyFrame kf = new KeyFrame(Duration.seconds(0.3), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(t -> {
                // Remove the old view after the animation completes
                parent.getChildren().remove(old);
            });
            timeline.play();
        } else {
            System.err.println("scene is null");
        }
    }

    // Navigate back to the previous view in the stack
    public static void goBack() throws IOException {
        navStack.removeLast(); // Remove current view from navigation stack
        loadRoot(navStack.getLast()); // Load the previous view
    }

    // Add a new view to the navigation stack and show it
    public static void addRoot(String fxml) throws IOException {
        loadRoot(fxml); // Load new view
        navStack.add(fxml); // Add to stack
    }

    // Set a new root view, clearing all previous navigation history
    public static void setRoot(String fxml) throws IOException {
        navStack.clear(); // Clear navigation stack
        addRoot(fxml);    // Load and push new root view
    }
}