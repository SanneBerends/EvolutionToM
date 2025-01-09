package controller.actions;


import model.File;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * A class that models the action when you press the 'initialize' button.
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class InitializeAction extends AbstractAction {
    private final File file;
    /**
     * The constructor that creates a new action to create a new environment
     */
    public InitializeAction(File file) {
        super("Initialize");
        this.file = file;
    }
    /*
    Methods
     */

    /**
     * A method that specifies what action should be taken
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        file.initializeExperiment();
    }
}
