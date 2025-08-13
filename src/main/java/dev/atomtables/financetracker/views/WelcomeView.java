package dev.atomtables.financetracker.views;

import dev.atomtables.financetracker.App;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeView implements Initializable {
    // Wave image views for animated background
    @FXML private ImageView waveImageView1;
    @FXML private ImageView waveImageView2;

    // Speed factor for wave movement
    private static final double WAVE_SPEED = 0.05;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make sure both waves are visible
        waveImageView1.setVisible(true);
        waveImageView2.setVisible(true);

        // Position wave 2 directly to the right of wave 1
        waveImageView2.setLayoutX(waveImageView1.getLayoutX() + waveImageView1.getFitWidth());

        // Start infinite horizontal wave movement animations for both images
        startWaveAnimation(waveImageView1, waveImageView2, false);
        startWaveAnimation(waveImageView2, waveImageView1, true);
    }

    /**
     * Starts a wave animation for the given wave image.
     *
     * @param currentWave The wave being animated.
     * @param nextWave The wave that follows currentWave (used to chain animations).
     * @param screw If true, applies reversed direction logic.
     */
    private void startWaveAnimation(ImageView currentWave, ImageView nextWave, boolean screw) {
        // Duration is proportional to image width and wave speed
        TranslateTransition transition = new TranslateTransition(
                Duration.seconds(currentWave.getFitWidth() / (WAVE_SPEED * 1000)), currentWave);

        // Set start and end positions for animation
        transition.setFromX(screw ? 1280 : 0);
        transition.setToX(screw ? 0 : -currentWave.getFitWidth());

        // Repeat indefinitely
        transition.setCycleCount(TranslateTransition.INDEFINITE);

        // This handler won't actually be called because INDEFINITE disables 'onFinished'
        transition.setOnFinished(event -> {
            currentWave.setTranslateX(screw ? 1280 : 0);
            currentWave.setLayoutX(nextWave.getLayoutX() + nextWave.getFitWidth());
            transition.playFromStart();
        });

        // Start animation
        transition.play();
    }

    // Triggered when the user clicks the setup button
    @FXML private void handleSetup() throws IOException {
        App.addRoot("setup/SetupView1"); // Navigate to setup screen
    }
}