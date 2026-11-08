/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import javax.swing.*;
import java.awt.*;

/**
 * The Base Class for all UI windows in MotorPH.
 * This ensures every window looks consistent.
 */
public abstract class mainMenuFunctions extends JFrame {

    public mainMenuFunctions(String title) {
        setTitle(title);
        setSize(800, 600); // Standard size for the app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Setting the application icon (optional)
        try {
            ImageIcon icon = new ImageIcon("src/view/assets/logo.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Logo not found, continue without icon
        }
    }

    /**
     * Every child class (AdminUI, RegularEmployeeUI) 
     * MUST implement this to build its specific screen.
     */
    protected abstract void initUI();

    protected final void logout() {
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
        new LoginFrame().setVisible(true);
    }
}
