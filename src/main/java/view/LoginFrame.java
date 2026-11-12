/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.LoginDao;
import dao.LoginCSVDao;
import model.UserAccount;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import interfaces.EmployeeDao;
import dao.EmployeeCSVDao;
import model.Employee;

public class LoginFrame extends JFrame {
    
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private final LoginDao loginDao;

    public LoginFrame() {
        this.loginDao = new LoginCSVDao();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH - Login");
        setSize(400, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(25, 25, 112)); 
        headerPanel.setPreferredSize(new Dimension(400, 160));
        
        JLabel titleLabel = new JLabel("MotorPH");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // --- Form Section ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(Color.WHITE);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(50, 40, 300, 20);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(userLabel);

        userField = new JTextField();
        userField.setBounds(50, 65, 300, 40);
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(userField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(50, 125, 300, 20);
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(passLabel);

        passField = new JPasswordField();
        passField.setBounds(50, 150, 300, 40);
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(passField);

        loginButton = new JButton("Login");
        loginButton.setBounds(50, 220, 300, 45);
        loginButton.setBackground(new Color(25, 25, 112));
        loginButton.setForeground(Color.BLACK); // Fixed to White for visibility
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginButton.addActionListener(this::handleLogin);
        formPanel.add(loginButton);

        // Allow Enter key to submit login credentials
        getRootPane().setDefaultButton(loginButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void handleLogin(ActionEvent e) {
        String inputId = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (inputId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both ID and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserAccount account = loginDao.authenticate(inputId, password);

        if (account != null) {
            openDashboard(account);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashboard(UserAccount account) {
        EmployeeDao empDao = new EmployeeCSVDao();
        
        // FIX: Trim the ID to ensure matching works even with CSV spacing
        String empId = account.getEmployeeNo().trim();
        System.out.println("DEBUG: Searching for Profile ID: '" + empId + "'");
        Employee emp = empDao.findById(empId);

        // SECURITY CHECK: Ensure the profile exists before opening ANY dashboard
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Profile not found for ID: " + empId + "\nPlease contact HR.", "System Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        String role = account.getRole();

        // Route the user to the correct window
        new RegularEmployeeUI(emp, role).setVisible(true);
        
        this.dispose(); 
    }
}
