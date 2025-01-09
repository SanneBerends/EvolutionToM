package view;


/**
 * The FileObserver interface, used to connect buttons, actions and the File class.
 *
 * @authors Sanne Berends
 * @date 2024
 */
public interface FileObserver {
    void onStateChanged(boolean newState);
    void onAgentsAdded();
    void onTimerChanged();
    void onInitializationChanged(int hasInitialized);
}
