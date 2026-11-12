package com.ecommerce.frontend;

import java.awt.Component;

/**
 * Interface decoupling user interface feedback dialogs from domain and controller logic.
 * Enables automated headless testing without GUI blocking or HeadlessExceptions.
 */
public interface UiFeedbackService {

    /**
     * Displays an informational message to the user.
     *
     * @param parent  Parent component for dialog anchoring
     * @param title   Dialog title bar text
     * @param message Informational text message
     */
    void showInformation(Component parent, String title, String message);

    /**
     * Displays a warning message to the user.
     *
     * @param parent  Parent component for dialog anchoring
     * @param title   Dialog title bar text
     * @param message Warning text message
     */
    void showWarning(Component parent, String title, String message);

    /**
     * Displays an error message to the user.
     *
     * @param parent  Parent component for dialog anchoring
     * @param title   Dialog title bar text
     * @param message Error text message
     */
    void showError(Component parent, String title, String message);

    /**
     * Displays a confirmation prompt requiring user approval.
     *
     * @param parent  Parent component for dialog anchoring
     * @param title   Dialog title bar text
     * @param message Confirmation prompt text message
     * @return true if user confirmed/approved, false otherwise
     */
    boolean showConfirmation(Component parent, String title, String message);
}
