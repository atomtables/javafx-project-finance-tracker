package dev.atomtables.financetracker.styles;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.control.skin.ButtonSkin;
import javafx.util.Duration;

/**
 * Custom button skin that adds hover and click animations:
 * - Fade effect on hover
 * - Scale effect on press and release
 */
public class ButtonStyle extends ButtonSkin {

    public ButtonStyle(Button control) {
        super(control);

        // Fade-in transition on mouse hover (reduces opacity slightly)
        final FadeTransition fadeIn = new FadeTransition(Duration.millis(100));
        fadeIn.setNode(control);
        fadeIn.setToValue(0.5);
        control.setOnMouseEntered(e -> fadeIn.playFromStart());

        // Fade-out transition when mouse exits (restores full opacity)
        final FadeTransition fadeOut = new FadeTransition(Duration.millis(100));
        fadeOut.setNode(control);
        fadeOut.setToValue(1.0);
        control.setOnMouseExited(e -> fadeOut.playFromStart());

        // Scale-down transition when button is pressed (shrinks slightly)
        final ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100));
        scaleIn.setNode(control);
        scaleIn.setToX(0.9);
        scaleIn.setToY(0.9);
        control.setOnMousePressed(e -> scaleIn.playFromStart());

        // Scale-up transition when button is released (restores original size)
        final ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100));
        scaleOut.setNode(control);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        control.setOnMouseReleased(e -> scaleOut.playFromStart());
    }
}