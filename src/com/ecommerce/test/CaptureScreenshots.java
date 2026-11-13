package com.ecommerce.test;

import com.ecommerce.frontend.EcommerceSwingApp;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility to capture high-resolution screenshots of the Swing Front-end tabs
 * for inclusion in the final project defense documentation and PDF report.
 */
public class CaptureScreenshots {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try {
                File dir = new File("docs/screenshots");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                EcommerceSwingApp app = new EcommerceSwingApp();
                app.setSize(1000, 700);
                app.doLayout();
                app.validate();

                // Find the JTabbedPane inside
                JTabbedPane tabs = findTabbedPane(app.getContentPane());
                if (tabs != null) {
                    for (int i = 0; i < tabs.getTabCount(); i++) {
                        tabs.setSelectedIndex(i);
                        app.doLayout();
                        app.validate();

                        BufferedImage image = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = image.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        app.paint(g2);
                        g2.dispose();

                        String name = "tab_" + (i + 1) + "_" + sanitizeName(tabs.getTitleAt(i)) + ".png";
                        File outFile = new File(dir, name);
                        ImageIO.write(image, "png", outFile);
                        System.out.println("Captured: " + outFile.getPath());
                    }
                }

                // Capture general app screenshot
                BufferedImage mainImg = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_RGB);
                Graphics2D gMain = mainImg.createGraphics();
                app.paint(gMain);
                gMain.dispose();
                ImageIO.write(mainImg, "png", new File(dir, "application_main_window.png"));
                System.out.println("Captured: docs/screenshots/application_main_window.png");

                app.dispose();
                System.out.println("All screenshots successfully captured.");
                System.exit(0);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static JTabbedPane findTabbedPane(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTabbedPane) {
                return (JTabbedPane) c;
            } else if (c instanceof Container) {
                JTabbedPane nested = findTabbedPane((Container) c);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static String sanitizeName(String title) {
        return title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }
}
