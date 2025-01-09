package controller;

import controller.buttons.NewButton;
import model.File;

import javax.swing.*;

/**
 * A class that models the FileButtonBar in which the buttons are located related to the file.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class FileButtonBar extends JMenuBar {


    /**
     * The constructor that creates a new FileButtonBar with all the necessary buttons.
     */
    public FileButtonBar(File file) {
        NewButton newButton = new NewButton(file);
        add(newButton);
    }

}