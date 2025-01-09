package controller.buttons;

import controller.actions.InitializeAction;
import model.File;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * A class that models the button that allows the user to initialize the environment.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class InitializeButton extends JButton {
    /**
     * The constructor that creates an 'initialize' button that calls the corresponding action when clicked.
     */
    public InitializeButton(File file) {
        super(new InitializeAction(file));
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
        setToolTipText("Initialize environment");
    }
}
