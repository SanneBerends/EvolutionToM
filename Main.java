import model.*;
import view.ProgramFrame;

/**
 * The main class, which initializes the GUI
 *
 * @authors Sanne Berends
 * @date 2024
 */
public class Main {
    /**
     * This method initializes the program and possibly the GUI.
     * @param args User input, used to determine if GUI should be initialized
     */
    public static void main(String[] args) {
        boolean GUI = true;
        String fileName = "result";
        int expType = 1;

        //Check if GUI parameter is provided and set to false
        if (args.length > 0 && args[0].equalsIgnoreCase("false")) {
            GUI = false;
        }

        // Check if an experiment type is given and save this
        if (args.length > 1) {
            expType = Integer.parseInt(args[1]);
        }

        // Check if a filename is given and save this
        if (args.length > 2) {
            fileName = args[2];
        }

        File file = new File(GUI, fileName, expType);
        // Initialize GUI only if the parameter is not set to false
        if (GUI) {
            new ProgramFrame(file);
        } else {
            file.initializeExperiment();
        }
    }


}
