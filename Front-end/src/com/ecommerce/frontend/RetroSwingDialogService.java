package com.ecommerce.frontend;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * 1990s retro-styled modal dialog service implementing UiFeedbackService.
 * Preserves strict aesthetic invariants:
 * - Header: Deep Navy Blue (#1A365D)
 * - Accent: Royal Blue (#2B6CB0)
 * - Borders: Subtle Gray (#CBD5E1) and EtchedBorder
 * - Background: Light Gray (#F5F7FA)
 * - Fonts: SansSerif (BOLD / PLAIN 12-14pt)
 * - ZERO emojis: Uses clean text markers [INFO], [WARNING], [ERROR], [CONFIRMATION]
 */
public class RetroSwingDialogService implements UiFeedbackService {

    private static final Color COLOR_NAVY_HEADER = new Color(26, 54, 93);     // #1A365D
    private static final Color COLOR_ROYAL_ACCENT = new Color(43, 108, 176);  // #2B6CB0
    private static final Color COLOR_BORDER_LINE = new Color(203, 213, 225);   // #CBD5E1
    private static final Color COLOR_PANEL_BG = new Color(245, 247, 250);     // #F5F7FA
    private static final Color COLOR_CHARCOAL_TEXT = new Color(45, 55, 72);   // #2D3748

    private static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 12);

    @Override
    public void showInformation(Component parent, String title, String message) {
        showRetroDialog(parent, "[INFO] " + title, message, DialogType.INFORMATION);
    }

    @Override
    public void showWarning(Component parent, String title, String message) {
        showRetroDialog(parent, "[WARNING] " + title, message, DialogType.WARNING);
    }

    @Override
    public void showError(Component parent, String title, String message) {
        showRetroDialog(parent, "[ERROR] " + title, message, DialogType.ERROR);
    }

    @Override
    public boolean showConfirmation(Component parent, String title, String message) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[HEADLESS PROMPT - CONFIRMATION AUTO-ACCEPTED] " + title + ": " + message);
            return true;
        }

        final boolean[] confirmed = new boolean[]{false};
        runOnEdt(() -> {
            Window owner = resolveWindow(parent);
            JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel rootPanel = createDialogPanel(dialog, "[CONFIRMATION] " + title, message, DialogType.CONFIRMATION, () -> {
                confirmed[0] = true;
                dialog.dispose();
            }, dialog::dispose);

            dialog.setContentPane(rootPanel);
            dialog.pack();
            adjustDialogDimensions(dialog, 420, 180);
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
        });

        return confirmed[0];
    }

    private enum DialogType {
        INFORMATION, WARNING, ERROR, CONFIRMATION
    }

    private void showRetroDialog(Component parent, String title, String message, DialogType type) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[HEADLESS DIALOG " + type + "] " + title + "\n" + message);
            return;
        }

        runOnEdt(() -> {
            Window owner = resolveWindow(parent);
            JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel rootPanel = createDialogPanel(dialog, title, message, type, dialog::dispose, null);

            dialog.setContentPane(rootPanel);
            dialog.pack();
            adjustDialogDimensions(dialog, 420, 180);
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
        });
    }

    private JPanel createDialogPanel(JDialog dialog, String title, String message, DialogType type,
                                     Runnable onOk, Runnable onCancel) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_PANEL_BG);
        root.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_LINE, 2));

        // 1. Retro Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_NAVY_HEADER);
        headerPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Message Body
        JPanel bodyPanel = new JPanel(new BorderLayout(10, 10));
        bodyPanel.setBackground(Color.WHITE);
        Border outerBorder = new EmptyBorder(12, 14, 12, 14);
        Border innerBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        bodyPanel.setBorder(new CompoundBorder(outerBorder, innerBorder));

        JPanel innerContent = new JPanel(new BorderLayout(8, 8));
        innerContent.setBackground(Color.WHITE);
        innerContent.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Type Badge
        String badgeText = switch (type) {
            case INFORMATION -> "[INFO]";
            case WARNING -> "[WARN]";
            case ERROR -> "[FAIL]";
            case CONFIRMATION -> "[QUERY]";
        };
        JLabel badgeLabel = new JLabel(badgeText);
        badgeLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        badgeLabel.setForeground(type == DialogType.ERROR ? new Color(180, 40, 40) :
                (type == DialogType.WARNING ? new Color(180, 100, 20) : COLOR_ROYAL_ACCENT));
        innerContent.add(badgeLabel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(message != null ? message : "");
        textArea.setFont(FONT_BODY);
        textArea.setForeground(COLOR_CHARCOAL_TEXT);
        textArea.setBackground(Color.WHITE);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(null);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        innerContent.add(scrollPane, BorderLayout.CENTER);

        bodyPanel.add(innerContent, BorderLayout.CENTER);
        root.add(bodyPanel, BorderLayout.CENTER);

        // 3. Bottom Button Bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttonBar.setBackground(COLOR_PANEL_BG);
        buttonBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER_LINE),
                new EmptyBorder(4, 8, 4, 8)));

        if (type == DialogType.CONFIRMATION) {
            JButton yesBtn = createRetroButton("Yes", onOk);
            JButton noBtn = createRetroButton("No", onCancel != null ? onCancel : dialog::dispose);
            buttonBar.add(yesBtn);
            buttonBar.add(noBtn);
            dialog.getRootPane().setDefaultButton(yesBtn);
        } else {
            JButton okBtn = createRetroButton("OK", onOk);
            buttonBar.add(okBtn);
            dialog.getRootPane().setDefaultButton(okBtn);
        }

        root.add(buttonBar, BorderLayout.SOUTH);
        return root;
    }

    private JButton createRetroButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(COLOR_PANEL_BG);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(4, 14, 4, 14)));
        btn.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
        return btn;
    }

    private static Window resolveWindow(Component c) {
        if (c == null) return null;
        if (c instanceof Window) return (Window) c;
        return SwingUtilities.getWindowAncestor(c);
    }

    private static void adjustDialogDimensions(JDialog dialog, int minW, int minH) {
        Dimension current = dialog.getSize();
        int w = Math.max(minW, Math.min(600, current.width + 40));
        int h = Math.max(minH, Math.min(400, current.height + 30));
        dialog.setSize(w, h);
    }

    private static void runOnEdt(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(task);
            } catch (InterruptedException | InvocationTargetException e) {
                Thread.currentThread().interrupt();
                System.err.println("[RetroSwingDialogService] Failed to dispatch dialog on EDT: " + e.getMessage());
            }
        }
    }
}
