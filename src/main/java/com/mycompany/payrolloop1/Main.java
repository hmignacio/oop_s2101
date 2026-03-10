/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.payrolloop1;

/**
 *
 * @author Vladimir Bernardo
 */
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import view.LoginFrame;

/**
 * Entry point for the MotorPH Payroll System.
 * Author: Vladimir Bernardo
 */
public class Main {

    public static void main(String[] args) {
        // 1. Set System Look and Feel for a modern native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException 
               | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Theme Error: " + e.getMessage());
        }

        // 2. Launch the Application on the Event Dispatch Thread (EDT)
        // This ensures thread safety for Swing components
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Initialize and display the Login Window
                    LoginFrame login = new LoginFrame();
                    login.setVisible(true);
                    
                    System.out.println("MotorPH System initialized successfully.");
                } catch (Exception e) {
                    System.err.println("Critical failure during launch: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
