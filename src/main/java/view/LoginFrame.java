/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import dao.LoginDao;
import dao.EmployeeDao;
import model.UserAccount;
import model.Employee;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Modern Login Frame for MotorPH.
 * Integrates with LoginDao for authentication and EmployeeDao for profile retrieval.
 */
public class LoginFrame extends JFrame {
    
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private LoginDao loginDao;

    public LoginFrame() {
        this.loginDao = new LoginDao();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH - Login");
        setSize(400, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Container
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(25, 25, 112)); // Midnight Blue
        headerPanel.setPreferredSize(new Dimension(400, 160));
        
        JLabel titleLabel = new JLabel("MotorPH");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // --- 2. Form Section ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(Color.WHITE);

        JLabel userLabel = new JLabel("Employee ID:");
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
        loginButton.setForeground(Color.BLACK); // Set text to white for contrast
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Event Listener
        loginButton.addActionListener(this::handleLogin);
        formPanel.add(loginButton);

        // Add panels to main container
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
        
        // Set Application Icon
        try {
            java.net.URL iconUrl = getClass().getResource("/view/assets/logo.png");
            if (iconUrl != null) setIconImage(new ImageIcon(iconUrl).getImage());
        } catch (Exception e) { /* Icon failed to load - ignore */ }
    }

    private void handleLogin(ActionEvent e) {
        String empId = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (empId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both ID and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Authenticate via LoginDao
        UserAccount account = loginDao.authenticate(empId, password);

        if (account != null) {
            this.dispose(); // Close login
            openDashboard(account);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Role-based redirection logic.
     * Ensures strict isolation between IT, HR, Finance, and Admin.
     */
    private void openDashboard(UserAccount account) {
        EmployeeDao empDao = new EmployeeDao();
        Employee emp = empDao.findById(account.getEmployeeNo());

        // Basic check for non-Admin users to ensure profile exists
        if (emp == null && !account.getRole().equalsIgnoreCase("Admin")) {
            JOptionPane.showMessageDialog(null, "Error: Profile data missing for ID " + account.getEmployeeNo());
            return;
        }

        String role = account.getRole();

        // STRICT ROLE ROUTING
        if (role.equalsIgnoreCase("Admin")) {
            new AdminUI().setVisible(true);
        } 
        else if (role.equalsIgnoreCase("HR")) {
            new HRUI().setVisible(true); 
        } 
        else if (role.equalsIgnoreCase("IT")) {
            // IT users get only the IT Management Portal
            new ITUI().setVisible(true);
        } 
        else if (role.equalsIgnoreCase("Finance")) {
            new FinanceUI().setVisible(true);
        }
        else {
            // Regular employees see only their specific self-service portal
            new RegularEmployeeUI(emp).setVisible(true);
        }
    }
}
