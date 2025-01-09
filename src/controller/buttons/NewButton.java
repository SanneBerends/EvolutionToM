package controller.buttons;

import controller.actions.NewAction;
import model.File;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * A class that models the button that allows the user to create a new environment.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class NewButton extends JButton {
    /**
     * The constructor that creates a 'new' button that calls the corresponding action when clicked.
     */
    public NewButton(File file) {
        super(new NewAction(file));
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
        setToolTipText("New environment");
    }
}
