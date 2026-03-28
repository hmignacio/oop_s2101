/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.payrolloop1;

/**
 *
 */
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import view.LoginFrame;

/**
 * Entry point for the MotorPH Payroll System.
 */
public class Main {

    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException 
               | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Theme Error: " + e.getMessage());
        }

        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    
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
