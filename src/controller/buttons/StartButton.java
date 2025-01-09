package controller.buttons;

import controller.actions.StartAction;
import model.File;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * A class that models the button that allows the user start the simulation.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class StartButton extends JButton {
    /**
     * The constructor that creates a 'start' button that calls the corresponding action when clicked.
     */
    public StartButton(File file) {
        super(new StartAction(file));
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
        setToolTipText("Start the simulation");
    }
}
