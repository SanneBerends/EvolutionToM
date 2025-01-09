package view;

import javax.swing.*;
import java.awt.*;

import controller.FileButtonBar;
import model.File;

/**
 * The ProgramFrame class, which models the frame of the GUI.
 *
 * @authors Sanne Berends
 * @date 2024
 */

public class ProgramFrame extends JFrame {
    /**
     * The constructor that creates a frame
     */
    public ProgramFrame(File file) {
        //Create the Frame
        super("Environment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1400, 900));
        setLocationRelativeTo(null);

        //Add menu
        setJMenuBar(new FileButtonBar(file));

        pack();
        setVisible(true);
        setResizable(false);

        //Create and add a panel to the frame
        ProgramPanel programPanel = new ProgramPanel(file);
        add(programPanel);
    }

}
