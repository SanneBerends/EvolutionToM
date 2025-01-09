package controller.buttons;

import controller.actions.PauseAction;
import model.File;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * A class that models the button that allows the user to load an environment.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class PauseButton extends JButton {
    /**
     * The constructor that creates a 'pause' button that calls the corresponding action when clicked.
     */
    public PauseButton(File file) {
        super(new PauseAction(file));
        setButtonProperties();
    }


    /*
    Methods
     */

    /**
     * Method that initialises the properties of this button
     */
    private void setButtonProperties() {
        setVerticalTextPosition(AbstractButton.CENTER);
        setHorizontalTextPosition(AbstractButton.CENTER);
        setMnemonic(KeyEvent.VK_S);
        setToolTipText("Pause the simulation");
    }
}
